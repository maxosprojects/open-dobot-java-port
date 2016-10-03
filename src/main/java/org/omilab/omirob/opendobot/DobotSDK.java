package org.omilab.omirob.opendobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.Math.abs;
import static org.omilab.omirob.opendobot.DobotKinematics.piHalf;
import static org.omilab.omirob.opendobot.DobotKinematics.piTwo;

/**
 * Created by Martin on 04.08.2016.
 */
public class DobotSDK implements IDobotSDK {
    private final static Logger logger = LoggerFactory.getLogger(DobotSDK.class);

    // See calibrate-accelerometers.py for details
    private final static float[] accelOffsets = {1024, 1024};

    // Backlash in the motor reduction gears is actually 22 steps, but 5 is visually unnoticeable.
// It is a horrible thing to compensate a bad backlash in software, but the only other
// option is to physically rebuild Dobot to fix this problem.
    private static final float backlash = 5f;

    // The NEMA 17 stepper motors that Dobot uses are 200 steps per revolution.
    private static final float stepperMotorStepsPerRevolution = 200.0f;
    // FPGA board has all stepper drivers"" stepping pins set to microstepping.
    private static final float baseMicrosteppingMultiplier = 16.0f;
    private static final float rearArmMicrosteppingMultiplier = 16.0f;
    private static final float frontArmMicrosteppingMultiplier = 16.0f;
    // The NEMA 17 stepper motors Dobot uses are connected to a planetary gearbox, the black cylinders
// with 10:1 reduction ratio
    private static float stepperPlanetaryGearBoxMultiplier = 10.0f;
    private float toolRotation;
    private float frontSteps;
    private float baseSteps;
    private float rearSteps;
    private int lastBaseDirection;
    private int lastRearDirection;
    private int lastFrontDirection;
    private DobotKinematics kinematics;
    private boolean debugOn;
    private boolean fake;
    private OpenDobotDriver driver;

    // calculate the actual number of steps it takes for each stepper motor to rotate 360 degrees
    float baseActualStepsPerRevolution = stepperMotorStepsPerRevolution * baseMicrosteppingMultiplier * stepperPlanetaryGearBoxMultiplier;
    float rearArmActualStepsPerRevolution = stepperMotorStepsPerRevolution * rearArmMicrosteppingMultiplier * stepperPlanetaryGearBoxMultiplier;
    float frontArmActualStepsPerRevolution = stepperMotorStepsPerRevolution * frontArmMicrosteppingMultiplier * stepperPlanetaryGearBoxMultiplier;
    private int gripper;

    public DobotSDK(int rate, String port, boolean debug, boolean fake, int timeout) throws IOException {
        this.debugOn = debug;
        this.fake = fake;
        this.driver = new OpenDobotDriver(port);
        init();
    }
    private void init() throws IOException {
        if (fake) {
            driver.ramps = true;
            driver.stepCoeff = 20000;
            driver.stopSeq = 0;
            driver.stepCoeffOver2 = driver.stepCoeff / 2;
            driver.freqCoeff = driver.stepCoeff * 25;
        } else {
            //driver.open(timeout);
        }
        kinematics = new DobotKinematics();
        toolRotation = 0f;
        gripper = 480;
        // Last directions to compensate backlash.
        lastBaseDirection = 0;
        lastRearDirection = 0;
        lastFrontDirection = 0;
        // Initialize arms current configuration from accelerometers
        if (fake) {
            baseSteps = 0L;
            rearSteps = 0L;
            frontSteps = 0L;
        } else
            InitializeAccelerometers();
    }

    @Override
    public void InitializeAccelerometers() throws IOException {
        logger.info("--=========--");
        logger.info("Initializing accelerometers");
        float rearAngle = 0;
        float frontAngle=0;
        if (driver.isFpga()) {
            // In FPGA v1.0 SPI accelerometers are read only when Arduino boots. The readings
            // are already available, so read once.
            short[] ret = {0, 0, 0, 0, 0, 0};
            ret = driver.GetAccelerometers();
            float accelRearX = ret[0];
            float accelFrontX = ret[3];
             rearAngle = piHalf - driver.accelToRadians(accelRearX, accelOffsets[0]);
             frontAngle = driver.accelToRadians(accelFrontX, accelOffsets[1]);
            logger.info(String.format("Initializing accelerometers result %.2f %.2f",rearAngle, frontAngle));

        } else {
            //    // In RAMPS accelerometers are on I2C bus and can be read at any time. We need to
            //    // read them multiple times to get average as MPU-6050 have greater resolution but are noisy.
            //    // However, due to the interference from the way A4988 holds the motors if none of the
            //    // recommended measures to suppress interference are in place (see open-dobot wiki), or
            //    // in case accelerometers are not connected, we need to give up and assume some predefined pose.
            //    accelRearX =0
            //    accelRearY =0
            //    accelRearZ =0
            //    accelFrontX =0
            //    accelFrontY =0
            //    accelFrontZ =0
            //    successes =0
            //            for
            //    i in
            //
            //    range(20):
            //    ret =(0,0,0,0,0,0,0)
            //    attempts =10
            //            while attempts:
            //    ret =self._driver.GetAccelerometers()
            //            if ret[0]:
            //    successes +=1
            //    accelRearX +=ret[1]
            //    accelRearY +=ret[2]
            //    accelRearZ +=ret[3]
            //    accelFrontX +=ret[4]
            //    accelFrontY +=ret[5]
            //    accelFrontZ +=ret[6]
            //            break
            //    attempts -=1
            //            if successes >0:
            //    divisor =
            //
            //    float(successes)
            //
            //    rearAngle =piHalf -self._driver.accel3DXToRadians(accelRearX /divisor,accelRearY /divisor,accelRearZ /divisor)
            //    frontAngle =-self._driver.accel3DXToRadians(accelFrontX /divisor,accelFrontY /divisor,accelFrontZ /divisor)
            //            else:
            //
            //    print("" Failed to read accelerometers.Make sure they are connected and interference is suppressed.See open-dobot wiki"")
            //
            //    print("" Assuming rear arm vertical and front arm horizontal"")
            //
            //    rearAngle =0
            //    frontAngle =-piHalf
        }
        rearSteps = (float) ((rearAngle /piTwo) *rearArmActualStepsPerRevolution +0.5);
        frontSteps = (float) ((frontAngle /piTwo) *frontArmActualStepsPerRevolution +0.5);
        driver.setCounters((int)baseSteps,(int)rearSteps,(int)frontSteps);
        logger.info(String.format("Initializing with steps: %f %f %f", baseSteps, rearSteps, frontSteps));
        Counters counters = driver.getCounters();
        logger.info(String.format("Reading back what was set: %d %d %d",counters.base,counters.rear,counters.front));
        float currBaseAngle =piTwo * baseSteps /  baseActualStepsPerRevolution;
        float currRearAngle = piHalf - piTwo * rearSteps / rearArmActualStepsPerRevolution;
        float currFrontAngle =piTwo * frontSteps / frontArmActualStepsPerRevolution;
        Point3f coords = kinematics.coordinatesFromAngles(currBaseAngle, currRearAngle, currFrontAngle);
        logger.info(String.format("Current estimated coordinates: %.2f %.2f %.2f",coords.x, coords.y, coords.z));
    }

    private void _moveArmToAngles() {
//    def _moveArmToAngles(self, baseAngle, rearArmAngle, frontArmAngle, duration):
//    self._baseAngle = baseAngle
//    self._rearAngle = rearArmAngle
//    self._frontAngle = frontArmAngle
//    dur = float(duration)
//
//		// baseStepLocation = long((baseAngle / 360.0) * baseActualStepsPerRevolution + 0.5)
//            // rearArmStepLocation = long((abs(rearArmAngle) / 360.0) * rearArmActualStepsPerRevolution + 0.5)
//            // frontArmStepLocation = long((abs(frontArmAngle) / 360.0) * frontArmActualStepsPerRevolution + 0.5)
//    baseStepLocation = long(baseAngle * baseActualStepsPerRevolution / piTwo)
//    rearArmStepLocation = long(rearArmAngle * rearArmActualStepsPerRevolution / piTwo)
//    frontArmStepLocation = long(frontArmAngle * frontArmActualStepsPerRevolution / piTwo)
//
//		self._debug("Base Step Location", baseStepLocation)
//            self._debug("Rear Arm Step Location", rearArmStepLocation)
//            self._debug("Frontarm Step Location", frontArmStepLocation)
//
//    baseDiff = baseStepLocation - self._baseSteps
//            rearDiff = rearArmStepLocation - self._rearSteps
//    frontDiff = frontArmStepLocation - self._frontSteps
//		self._debug(""baseDiff"", baseDiff)
//            self._debug(""rearDiff"", rearDiff)
//            self._debug(""frontDiff"", frontDiff)
//
//    self._baseSteps = baseStepLocation
//    self._rearSteps = rearArmStepLocation
//    self._frontSteps = frontArmStepLocation
//
//            baseDir = 1
//    rearDir = 1
//    frontDir = 1
//
//            if (baseDiff < 1):
//    baseDir = 0
//            if (rearDiff < 1):
//    rearDir = 0
//            if (frontDiff > 1):
//    frontDir = 0
//
//    baseSliced = self._sliceStepsToValues(abs(baseDiff), dur)
//    rearSliced = self._sliceStepsToValues(abs(rearDiff), dur)
//    frontSliced = self._sliceStepsToValues(abs(frontDiff), dur)
//
//            for base, rear, front in zip(baseSliced, rearSliced, frontSliced):
//    ret = [0, 0]
//            // If ret[0] == 0 then command timed out or crc failed.
//			// If ret[1] == 0 then command queue was full.
//			while ret[0] == 0 or ret[1] == 0:
//    ret = self._driver.Steps(base, rear, front, baseDir, rearDir, frontDir)
    }

    private float[] moveToAnglesSlice(float baseAngle, float rearArmAngle, float frontArmAngle, int toolRotation) throws IOException {
        float baseStepLocation = baseAngle * baseActualStepsPerRevolution / piTwo;
        float rearArmStepLocation = abs(rearArmAngle * rearArmActualStepsPerRevolution / piTwo);
        float frontArmStepLocation = abs(frontArmAngle * frontArmActualStepsPerRevolution / piTwo);

        logger.debug(String.format("Base Step Location %f", baseStepLocation));
        logger.debug(String.format("Rear Arm Step Location %f", rearArmStepLocation));
        logger.debug(String.format("Front Arm Step Location %f", frontArmStepLocation));
        logger.debug(String.format("self._baseSteps %f", baseSteps));
        logger.debug(String.format("self._rearSteps %f", rearSteps));
        logger.debug(String.format("self._frontSteps %f", frontSteps));
        float baseDiff = baseStepLocation - baseSteps;
        float rearDiff = rearArmStepLocation - rearSteps;
        float frontDiff = frontArmStepLocation - frontSteps;

        logger.debug(String.format("baseDiff %f", baseDiff));
        logger.debug(String.format("rearDiff %f", rearDiff));
        logger.debug(String.format("frontDiff %f", frontDiff));

        int baseSign = 1;
        int rearSign = 1;
        int frontSign = -1;
        int baseDir = 1;
        int rearDir = 1;
        int frontDir = 1;

        if (baseDiff < 1) {
            baseDir = 0;
            baseSign = -1;
        }
        if (rearDiff < 1) {
            rearDir = 0;
            rearSign = -1;
        }
        if (frontDiff > 1) {
            frontDir = 0;
            frontSign = 1;
        }


        float baseDiffAbs = abs(baseDiff);
        float rearDiffAbs = abs(rearDiff);
        float frontDiffAbs = abs(frontDiff);

        CmdVal base = driver.stepsToCmdValFloat(baseDiffAbs);
        CmdVal rear = driver.stepsToCmdValFloat(rearDiffAbs);
        CmdVal front = driver.stepsToCmdValFloat(frontDiffAbs);

        // Compensate for backlash.
        // For now compensate only backlash in the base motor as the backlash in the arm motors depends
        // on specific task (a laser/brush or push-pull tasks).
        if (lastBaseDirection != baseDir && base.steps > 0) {
            CmdVal tmp = driver.stepsToCmdValFloat(baseDiffAbs + backlash);
            base.cmd = tmp.cmd;
            lastBaseDirection = baseDir;
        }

        // if self._lastRearDirection != rearDir and actualStepsRear > 0:
        // 	cmdRearVal, _ignore, _ignore = self._driver.stepsToCmdValFloat(rearDiffAbs + backlash)
        // 	self._lastRearDirection = rearDir
        // if self._lastFrontDirection != frontDir and actualStepsFront > 0:
        // 	cmdFrontVal, _ignore, _ignore = self._driver.stepsToCmdValFloat(frontDiffAbs + backlash)
        // 	self._lastFrontDirection = frontDir

        if (!fake) {
            // Repeat until the command is queued. May not be queued if queue is full.
            while (true) {
                byte ret = driver.steps(base.cmd, rear.cmd, front.cmd, baseDir, rearDir, frontDir,
                        (short) gripper, (short) toolRotation);
                if(ret==1)
                    break;;
            }
            //}
        }
        return new float[]{
            base.steps * baseSign,
            rear.steps * rearSign,
            front.steps * frontSign,
            (base.leftOver * baseSign),
            (rear.leftOver * rearSign),
            (front.leftOver * frontSign)};
    }

    private int freqToCmdVal(float freq) {
        return driver.freqToCmdVal(freq);
    }

    @Override
    public void moveWithSpeed(float xx, float yy, float zz, float maxVel, float accel, float toolRotation) throws IOException {
        if (toolRotation > 1024)
            toolRotation = 1024f;
        if (toolRotation < 0)
            toolRotation = 0;

        if (accel == 0)
            accel = maxVel;

        logger.debug("--=========--");
        logger.debug("maxVel", maxVel);
        logger.debug("accelf", accel);

        float currBaseAngle = piTwo * baseSteps / baseActualStepsPerRevolution;
        float currRearAngle = piHalf - piTwo * rearSteps / rearArmActualStepsPerRevolution;
        float currFrontAngle = piTwo * frontSteps / frontArmActualStepsPerRevolution;
        Point3f currPos = DobotKinematics.coordinatesFromAngles(currBaseAngle, currRearAngle, currFrontAngle);

        float vectX = xx - currPos.x;
        float vectY = yy - currPos.y;
        float vectZ = zz - currPos.z;
        logger.debug(String.format("moving from %f,%f,%f", currPos.x, currPos.y, currPos.z));
        logger.debug(String.format("moving to %f %f %f", xx, yy, zz));
        logger.debug(String.format("moving by %f %f %f", vectX, vectY, vectZ));

        float distance = (float) Math.sqrt(Math.pow(vectX, 2) + Math.pow(vectY, 2) + Math.pow(vectZ, 2));
        logger.debug("distance to travel", distance);

        // If half the distance is reached before reaching maxSpeed with the given acceleration, then actual
        // maximum velocity will be lower, hence total number of slices is determined from half the distance
        // and acceleration.
        float distToReachMaxSpeed = (float) (Math.pow(maxVel, 2) / (2.0 * accel));
        int slices = 0;
        int accelSlices = 0;
        float timeToAccel = 0;
        float timeFlat = 0;
        int flatSlices = 0;

        if (distToReachMaxSpeed * 2.0 >= distance) {
            timeToAccel = (float) Math.sqrt(distance / accel);
            accelSlices = (int) (timeToAccel * 50.0);
            timeFlat = 0;
            flatSlices = 0;
            maxVel = (float) Math.sqrt(distance * accel);
        }


        // Or else number of slices when velocity does not change is greater than zero.
        else {
            timeToAccel = maxVel / accel;
            accelSlices = (int) (timeToAccel * 50.0f);
            timeFlat = (float) ((distance - distToReachMaxSpeed * 2.0) / maxVel);
            flatSlices = (int) (timeFlat * 50.0f);
        }

        slices = (int) (accelSlices * 2.0 + flatSlices);
        logger.debug(String.format("slices to do: %d", slices));
        logger.debug(String.format("accelSlices: %d", accelSlices));
        logger.debug(String.format("flatSlices: %d", flatSlices));

        // Acceleration/deceleration in respective axes
        float accelX = (accel * vectX) / distance;
        float accelY = (accel * vectY) / distance;
        float accelZ = (accel * vectZ) / distance;
        logger.debug("accelXYZ", accelX, accelY, accelZ);

        // Vectors in respective axes to complete acceleration/deceleration
        float segmentAccelX = (float) (accelX * Math.pow(timeToAccel, 2) / 2.0);
        float segmentAccelY = (float) (accelY * Math.pow(timeToAccel, 2) / 2.0);
        float segmentAccelZ = (float) (accelZ * Math.pow(timeToAccel, 2) / 2.0);
        logger.debug(String.format("segmentAccelXYZ %.2f %.2f %.2f", segmentAccelX, segmentAccelY, segmentAccelZ));

        // Maximum velocity in respective axes for the segment with constant velocity
        float maxVelX = (maxVel * vectX) / distance;
        float maxVelY = (maxVel * vectY) / distance;
        float maxVelZ = (maxVel * vectZ) / distance;
        logger.debug(String.format("maxVelXYZ %.2f %.2f %.2f", maxVelX, maxVelY, maxVelZ));

        // Vectors in respective axes for the segment with constant velocity
        float segmentFlatX = maxVelX * timeFlat;
        float segmentFlatY = maxVelY * timeFlat;
        float segmentFlatZ = maxVelZ * timeFlat;
        logger.debug(String.format("segmentFlatXYZ %.2f %.2f %.2f", segmentFlatX, segmentFlatY, segmentFlatZ));
        float segmentToolRotation = (toolRotation - toolRotation) / slices;
        logger.debug(String.format("segmentToolRotation %.2f", segmentToolRotation));

        int commands = 1;
        float leftStepsBase = 0.0f;
        float leftStepsRear = 0.0f;
        float leftStepsFront = 0.0f;
        while (commands < slices) {
            logger.debug("==============================");
            logger.debug(String.format("slice //%d", commands));
            float t2half;
            float nextX;
            float nextY;
            float nextZ;
            // If accelerating
            if (commands <= accelSlices) {
                t2half = (float) (Math.pow(commands / 50.0, 2) / 2.0);
                nextX = currPos.x + accelX * t2half;
                nextY = currPos.y + accelY * t2half;
                nextZ = currPos.z + accelZ * t2half;
            }
            // If decelerating
            else if (commands >= accelSlices + flatSlices) {
                t2half = (float) (Math.pow((slices - commands) / 50.0, 2) / 2.0);
                nextX = (float) (currPos.x + segmentAccelX * 2.0 + segmentFlatX - accelX * t2half);
                nextY = (float) (currPos.y + segmentAccelY * 2.0 + segmentFlatY - accelY * t2half);
                nextZ = (float) (currPos.z + segmentAccelZ * 2.0 + segmentFlatZ - accelZ * t2half);
            }
            // Or else moving at maxSpeed
            else {
                float t = (float) (abs(commands - accelSlices) / 50.0);
                nextX = currPos.x + segmentAccelX + maxVelX * t;
                nextY = currPos.y + segmentAccelY + maxVelY * t;
                nextZ = currPos.z + segmentAccelZ + maxVelZ * t;
            }
            logger.debug(String.format("moving to %f %f %f", nextX, nextY, nextZ));

            float nextToolRotation = toolRotation + (segmentToolRotation * commands);
            logger.debug(String.format("nextToolRotation %.2f", nextToolRotation));

            Angle3f angles = DobotKinematics.anglesFromCoordinates(nextX, nextY, nextZ);

            float[] r = moveToAnglesSlice(angles.x, angles.y, angles.z, (int) nextToolRotation);
            float movedStepsBase = r[0];
            float movedStepsRear = r[1];
            float movedStepsFront = r[2];
            leftStepsBase = r[3];
            leftStepsRear = r[4];
            leftStepsFront = r[5];


            logger.debug(String.format("moved %.2f %.2f %.2f steps", movedStepsBase, movedStepsRear, movedStepsFront));
            logger.debug(String.format("leftovers %.2f %.2f %.2f", leftStepsBase, leftStepsRear, leftStepsFront));

            commands += 1;
            baseSteps += movedStepsBase;
            rearSteps += movedStepsRear;
            frontSteps += movedStepsFront;

            currBaseAngle = piTwo * baseSteps / baseActualStepsPerRevolution;
            currRearAngle = piHalf - piTwo * rearSteps / rearArmActualStepsPerRevolution;
            currFrontAngle = piTwo * frontSteps / frontArmActualStepsPerRevolution;
            Point3f c = kinematics.coordinatesFromAngles(currBaseAngle, currRearAngle, currFrontAngle);
        }
    }

//    def Gripper(self, gripper):
//            if gripper > 480:
//    self._gripper = 480
//    float gripper < 208:
//    self._gripper = 208
//            else:
//    self._gripper = gripper
//
//		self._driver.Steps(0, 0, 0, 0, 0, 0, self._gripper, self._toolRotation)
//    }
//
//
//    def Wait(self, waitTime):
//            """"""
//    See description in DobotDriver.Wait()
//		""""""
//                self._driver.Wait(waitTime)
//
//    def CalibrateJoint(self, joint, forwardCommand, backwardCommand, direction, pin, pinMode, pullup):
//            """"""
//    See DobotDriver.CalibrateJoint()
//		""""""
//                return self._driver.CalibrateJoint(joint, forwardCommand, backwardCommand, direction, pin, pinMode, pullup)
//
//    def EmergencyStop(self):
//            """"""
//    See DobotDriver.EmergencyStop()
//		""""""
//
//                return self._driver.EmergencyStop()
//
//    def LaserOn(self, on):
//            return self._driver.LaserOn(on)
//
//    def PumpOn(self, on):
//            return self._driver.PumpOn(on)
//
//    def ValveOn(self, on):
//            return self._driver.ValveOn(on)

        @Override
        public void pumpOn(boolean on) throws IOException {
            driver.pumpOn(on);
        }

    @Override
    public void valveOn(boolean on) throws IOException {
        driver.valveOn(on);
    }

    @Override
    public void reset() throws IOException {
        driver.reset();
        init();
    }

    public void calibrate() throws IOException {

        driver.calibrateJoint(1,
                freqToCmdVal(50),
                freqToCmdVal(50),
                0,
                0,
                1,
                0);
    }
}