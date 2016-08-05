package org.omilab.omirob.opendobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin on 04.08.2016.
 */
public class DobotKinematics {
    private final static Logger logger = LoggerFactory.getLogger(DobotKinematics.class);
    // Dimentions in mm
    private final static float lengthRearArm = 135.0f;
    private final static float lengthFrontArm = 160.0f;
    // Horizontal distance from Joint3 to the center of the tool mounted on the end effector.
    private final static float distanceTool = 50.9f;
    // Joint1 height.
    private final static float heightFromGround = 80.0f + 23.0f;

    private final static float lengthRearSquared = (float) Math.pow(lengthRearArm, 2f);
    private final static float lengthFrontSquared = (float) Math.pow(lengthFrontArm, 2);

    private final static float armSquaredConst = (float) (Math.pow(lengthRearArm, 2) + Math.pow(lengthFrontArm, 2));
    private final static float armDoubledConst = (float) (2.0 * lengthRearArm * lengthFrontArm);
    private final static float radiansToDegrees = (float) (180.0f / Math.PI);
    private final static float degreesToRadians = (float) (Math.PI / 180.0f);

    public final static float piHalf = (float) (Math.PI / 2.0);
    public final static float piTwo = (float) (Math.PI * 2.0);
    public final static float piThreeFourths = (float) (Math.PI * 3.0 / 4.0);


    public DobotKinematics() {
//        def __init__(self, debug=False):
//        logger.debugOn = debug
//
//        def _debug(self, *args):
//                if logger.debugOn:
//                // Since "print" is not a function the expansion (*) cannot be used
//                // as it is not an operator. So this is a workaround.
//                for arg in args:
//                sys.stdout.write(str(arg))
//                sys.stdout.write(" ")
//        print("")
    }

   public  static Point3f coordinatesFromAngles(float baseAngle, float rearArmAngle, float frontArmAngle) {
        float radius = (float) (lengthRearArm * Math.cos(rearArmAngle) + lengthFrontArm * Math.cos(frontArmAngle) + distanceTool);
        float x = (float) (radius * Math.cos(baseAngle));
        float y = (float) (radius * Math.sin(baseAngle));
        float z = (float) (heightFromGround - lengthFrontArm * Math.sin(frontArmAngle) + lengthRearArm * Math.sin(rearArmAngle));
        return new Point3f(x, y, z);
    }

    //    http://www.learnaboutrobots.com/inverseKinematics.htm

    public static Angle3f anglesFromCoordinates(float x, float y, float z) {
        // Radius to the center of the tool.
        float radiusTool = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        logger.debug(String.format("radiusTool %f", radiusTool));
        // Radius to joint3.
        float radius = radiusTool - distanceTool;
        logger.debug(String.format("radius %f", radius));
        float baseAngle = (float) Math.atan2(y, x);
        logger.debug(String.format("ik base angle %f", baseAngle));
        // X coordinate of joint3.
        float jointX = (float) (radius * Math.cos(baseAngle));
        logger.debug(String.format("jointX %f", jointX));
        // Y coordinate of joint3.
        float jointY = (float) (radius * Math.sin(baseAngle));
        logger.debug(String.format("jointY %f", jointY));
        float actualZ = z - heightFromGround;
        logger.debug(String.format("actualZ %f", actualZ));
        // Imaginary segment connecting joint1 with joint2, squared.
        float hypotenuseSquared = (float) (Math.pow(actualZ, 2) + Math.pow(radius, 2));
        float hypotenuse = (float) Math.sqrt(hypotenuseSquared);
        logger.debug(String.format("hypotenuse %f", hypotenuse));
        logger.debug(String.format("hypotenuseSquared %f", hypotenuseSquared));

        float q1 = (float) Math.atan2(actualZ, radius);
        logger.debug(String.format("q1 %f", q1));
        float q2 = (float) Math.acos((lengthRearSquared - lengthFrontSquared + hypotenuseSquared) / (2.0 * lengthRearArm * hypotenuse));
        logger.debug(String.format("q2 %f", q2));
        float rearAngle = piHalf - (q1 + q2);
        logger.debug(String.format("ik rear angle %f", rearAngle));
        float frontAngle = (float) (piHalf - (Math.acos((lengthRearSquared + lengthFrontSquared - hypotenuseSquared) / (2.0 * lengthRearArm * lengthFrontArm)) - rearAngle));
        logger.debug(String.format("ik front angle %f", frontAngle));
        return new Angle3f(baseAngle, rearAngle, frontAngle);
    }

    public float get_distance_from_origin_to_cartesian_point_3D(float x, float y, float z) {
        //get distance from origin (0,0,0) to end point in 3D using pythagorean thm in 3D; distance = sqrt(x^2+y^2+z^2)
        float distanceToEndPoint = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
        return distanceToEndPoint;
    }

    // angles passed as arguments here should be real world angles (horizontal = 0, below is negative, above is positive)
    // i.e. they should be set up the same way as the unit circle is
    public boolean check_for_angle_limits_is_valid(float baseAngle, float rearArmAngle, float foreArmAngle) {
        boolean ret = true;
        // implementing limit switches and IMUs will make this function more accurate and allow the user to calibrate the limits
        // necessary for this function.
        // Not currently checking the base angle

        // check the rearArmAngle
        // max empirically determined to be around 107 - 108 degrees. Using 105.
        // min empirically determined to be around -23/24 degrees. Using -20.
        if (-20 > rearArmAngle && foreArmAngle > 105) {
            logger.warn("Rear arm angle out of range");
            ret = false;
        }

        // check the foreArmAngle
        // the valid forearm angle is dependent on the rear arm angle. The real world angle of the forearm
        // (0 degrees = horizontal) needs to be evaluated.
        // min empirically determined to be around -105 degrees. Using -102.
        // max empirically determined to be around 21 degrees. Using 18.
        if (-102 > foreArmAngle && foreArmAngle > 18) {
            logger.warn("Fore arm angle out of range");
            ret = false;
        }
        return ret;
    }
}
