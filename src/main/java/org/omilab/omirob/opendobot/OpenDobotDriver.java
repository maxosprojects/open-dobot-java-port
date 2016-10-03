package org.omilab.omirob.opendobot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.omilab.omirob.opendobot.DobotKinematics.piHalf;

/**
 * Created by Martin on 02.08.2016.
 */
public class OpenDobotDriver {
    private final static Logger logger = LoggerFactory.getLogger(OpenDobotDriver.class);
    private static final int TRIES = 10;
    private static final long TIMEOUT = 1000;

    private final int toolRotation=0;
    private final int gripper=480;
    static int stepCoeff = 500000;
    private final String portName;
    private OutputStream out;
    private InputStream in;
    private int crc=0xffff;
    private final static byte CMD_READY = 0;
    private final static byte CMD_STEPS = 1;
    private final static byte CMD_EXEC_QUEUE = 2;
    private final static byte CMD_GET_ACCELS = 3;
    private final static byte CMD_SWITCH_TO_ACCEL_REPORT_MODE = 4;
    private final static byte CMD_CALIBRATE_JOINT = 5;
    private final static byte CMD_EMERGENCY_STOP = 6;
    private final static byte CMD_SET_COUNTERS = 7;
    private final static byte CMD_GET_COUNTERS = 8;
    private final static byte CMD_LASER_ON = 9;
    private final static byte CMD_PUMP_ON = 10;
    private final static byte CMD_VALVE_ON = 11;
    private final static byte CMD_BOARD_VERSION = 12;
    static int stopSeq=0x0242f000;
    public boolean ramps=false;
    public int stepCoeffOver2;
    public int freqCoeff= stepCoeff * 25;
    private boolean fpga=true;
    private SerialPort port;

    private  void crc_clear(){
        crc = 0xffff;
    }

    private void crc_update(byte data){
        crc = crc ^ ((data&0xFF) << 8);
        for(int bit=0;bit<8;bit++){ //for bit in range(0, 8):
            if ((crc&0x8000) == 0x8000)
                crc = (crc << 1) ^ 0x1021;
            else
                crc = crc << 1;
        }
    }

    private void writebyte(byte val) throws IOException {
        crc_update(val);
        out.write(val&0xFF);
    }

    private void writeword(short val) throws IOException {
        writebyte((byte) ((val&0xFF00)>>8));
        writebyte((byte) (val&0xFF));
    }

    private void writelong(int val) throws IOException {
        writebyte((byte) ((val&0xFF000000)>>24));
        writebyte((byte) ((val&0xFF0000)>>16));
        writebyte((byte) ((val&0xFF00)>>8));
        writebyte((byte) (val&0xFF));
    }

    private byte readbyte() throws IOException {
        int data=-1;
        long startTime=System.currentTimeMillis();
        while(data<0&&(System.currentTimeMillis()-startTime<TIMEOUT)) //enforce serial timeout
            data=in.read();
        if(data<0)
            throw new IOException("readbyte() timeout");

        crc_update((byte) (data&0xFF));
        return (byte) (data&0xFF);
    }

    private short readword() throws IOException {
        byte val1 = readbyte();
        byte val2 = readbyte();
        return (short) ((val1&0xFF)<<8|(val2&0xFF));
    }
    private int readlong() throws IOException {
        byte val1 = readbyte();
        byte val2 = readbyte();
        byte val3 = readbyte();
        byte val4 = readbyte();
        return (int) ((val1&0xFF)<<24|
                (val2&0xFF)<<16|
                (val3&0xFF)<<8|
                (val4&0xFF));
    }

    private void sendcommand(byte command) throws IOException {
        crc_clear();
        while(in.available()>0)
            in.read();
        writebyte(command);
    }

    private byte[] boardVersion() throws IOException {
        byte[] res=read(CMD_BOARD_VERSION,1);
        return res;
    }

    private byte[] read(byte cmd, int length) throws IOException {
        int trys=10;
        while (trys>0) {
            sendcommand(cmd);
            writechecksum();
            out.flush();
            byte[] ret=new byte[length];
            for (int i = 0; i < length; i++) {
                int val = in.read();
                ret[i] = (byte) (val & 0xFF);
                crc_update(ret[i]);
            }
            int[] crcword = readchecksumword();
            if (crcword[0]!=1)
                if((crc&0xFFFF) == (crcword[1]&0xFFFF))
                    return ret;
            trys -= 1;
        }
        throw new RuntimeException("CRC failed ");
    }

    public static int stepsToCmdVal(int steps) {
        steps=Math.abs(steps);
        if (steps == 0)
            return stopSeq;
        return Integer.reverseBytes(stepCoeff / steps);
    }

    public byte steps(int j1, int j2, int j3, int j1dir, int j2dir, int j3dir, short servoGrab, short servoRot) throws IOException {
        int trys=10;
        byte control = (byte) ((j1dir & 0x01) |
                ((j2dir & 0x01) << 1) |
                ((j3dir & 0x01) << 2));
        logger.debug(String.format("j1dir: %d, j2dir: %d, j3dir: %d", j1dir, j2dir, j3dir));
        logger.debug(String.format("j1: %d, j2: %d, j3: %d", j1, j2, j3));
        while (trys>0) {
            sendcommand(CMD_STEPS);
            writelong(j1);
            writelong(j2);
            writelong(j3);
            writebyte(control);
            writeword(Short.reverseBytes(servoGrab));
            writeword(Short.reverseBytes(servoRot));
            writechecksum();
            out.flush();
            byte res = readbyte();
            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return res;
            trys--;
        }
        logger.warn("CRC error");
        return -1;
    }

    public void steps(int j1, int j2, int j3, short servoGrab, short servoRot) throws IOException {
        int j1dir=(j1<0?1:0);
        int j2dir=(j2<0?1:0);
        int j3dir=(j3<0?1:0);
        byte control = (byte)   ((j1dir & 0x01) |
                                ((j2dir & 0x01) << 1) |
                                ((j3dir & 0x01) << 2));
        sendcommand(CMD_STEPS);
        writelong(stepsToCmdVal(j1));
        writelong(stepsToCmdVal(j2));
        writelong(stepsToCmdVal(j3));
        writebyte(control);
        writeword(Short.reverseBytes(servoGrab));
        writeword(Short.reverseBytes(servoRot));
        writechecksum();
        byte res=readbyte();
        if((crc & 0xFFFF)!= (crc & 0xFFFF)) {
            throw new RuntimeException("CRC error");
        }
    }
    public void  setCounters(int base, int rear, int fore) throws IOException {
        int trys=10;
        while(trys>0)
        {
            sendcommand(CMD_SET_COUNTERS);
            writelong(base);
            writelong(rear);
            writelong(fore);
            writechecksum();
            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return;
            trys--;
        }
        logger.warn("CRC error");
    }

    public Counters  getCounters() throws IOException {
        int trys=10;
        while(trys>0)
        {
            sendcommand(CMD_GET_COUNTERS);
            writechecksum();
            int base=readlong();
            int rear=readlong();
            int front=readlong();

            Counters c=new Counters(front, rear, base);
            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return c;
            trys--;
        }
        logger.warn("CRC error");
        return null;
    }

    public void PumpOn(boolean on) {
       // writebyte();
    }

    private int[] readchecksumword() throws IOException {
        int ret[]=new int[2];
        int lower=in.read();
        int upper=in.read();
        if(lower<0||upper<0)
            ret[0]=1;

        int crcw=(lower&0xFF)<<8;
        crcw=crcw|(upper&0xFF);
        ret[1]= (crcw);
        return ret;
    }

    private void writechecksum() throws IOException {
        out.write((crc&0xFF00)>>8);
        out.write(crc&0xFF);
    }

    public OpenDobotDriver(String portname){
        this.portName=portname;
        init(portname);
    }

    private void init(String portname){
        try {
            CommPortIdentifier portid = CommPortIdentifier.getPortIdentifier(portname);
            port = (SerialPort) portid.open("asdf", 1000);

            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            port.enableReceiveThreshold(1);
            port.enableReceiveTimeout((int) TIMEOUT);
            out = port.getOutputStream();
            in = port.getInputStream();

            port.setDTR(true);
            port.setRTS(true);
            Thread.sleep(1000);
            byte[] bv=boardVersion();
            System.out.println(bv[0]);
        } catch (PortInUseException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (UnsupportedCommOperationException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
    }

    public boolean isFpga() {
        return fpga;
    }

    public CmdVal stepsToCmdValFloat(float steps) {

        //        Converts number of steps for dobot to do in 20ms into a command value that dobot
        //        takes to set the stepping frequency.
        //
        //        @param steps - float number of steps; float to minimize error and have finer control
        //		@return tuple (command_value, leftover), where leftover is the fractioned steps that don't fit
        //        into 20ms interval a command runs for
        //        '''
        if (Math.abs(steps) < 0.01)
            return (new CmdVal(stopSeq, 0, 0.0f));
		// "round" makes leftover negative in certain cases and causes backlash compensation to oscillate.
		// actualSteps = long(round(steps))
        int actualSteps = (int) steps;
        if(actualSteps == 0)
            return (new CmdVal(stopSeq, 0, steps));
        int val = (int) (stepCoeff / actualSteps);
        actualSteps = stepCoeff / val;
        if (val == 0)
            return new CmdVal(stopSeq, 0, steps);
        return new CmdVal(Integer.reverseBytes(val), actualSteps, steps - actualSteps);
    }

    public short[] GetAccelerometers() throws IOException {

//        '''
//        Returns data aquired from accelerometers at power on.
//        There are 17 reads in FPGA version and 20 reads in RAMPS version of each accelerometer
//        that the firmware does and then averages the result before returning it here.

//        '''
        sendcommand(CMD_GET_ACCELS);
        writechecksum();
        short[] ret=new short[6];
        for(int i=0;i<6;i++)
            ret[i]=readword();
        int[] crcword = readchecksumword();
        if (crcword[0] != 1)
            if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                return ret;
        throw new RuntimeException("CRC failed");
    }

    public float accelToRadians(float val, float offset) {
        float res= (float) Math.asin((val - offset) / 493.56f);
        if(Float.isNaN(res))
            return piHalf;
        return res;
    }

    public boolean calibrateJoint(int joint, int forwardCommand, int backwardCommand, int direction, int pin, int pinMode, int pullup) throws IOException {
//    Initiates joint calibration procedure using a limit switch/photointerrupter. Effective immediately.
//    Current command buffer is cleared.
//    Cancel the procedure by issuing EmergencyStop() is necessary.
//
//    @param joint - which joint to calibrate: 1-3
//    @param forwardCommand - command to send to the joint when moving forward (towards limit switch);
//    use freqToCmdVal()
//    @param backwardCommand - command to send to the joint after hitting  (towards limit switch);
//    use freqToCmdVal()
//    @param direction - direction to move joint towards limit switch/photointerrupter: 0-1
//    @param pin - firmware internal pin reference number that limit switch is connected to;
//    refer to dobot.h -> calibrationPins
//    @param pinMode - limit switch/photointerrupter normal LOW = 0, normal HIGH = 1
//    @param pullup - enable pullup on the pin = 1, disable = 0
//    @return True if command succesfully received, False otherwise.

        if (1 > joint || joint > 3)
            return false;
        byte control = (byte) (((pinMode & 0x01) << 4) | ((pullup & 0x01) << 3) | ((direction & 0x01) << 2) | ((joint - 1) & 0x03));
        int trys=10;
        while(trys>0)
        {
            sendcommand(CMD_CALIBRATE_JOINT);
            writelong(forwardCommand);
            writelong(backwardCommand);
            writebyte((byte) pin);
            writebyte(control);
            writechecksum();
            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return true;
            trys--;
        }
        logger.warn("CRC error");
        return false;
    }


    public int freqToCmdVal(float freq) {
//        '''
//        Converts stepping frequency into a command value that dobot takes.
//        '''
        if (freq == 0)
            return stopSeq;
        return Integer.reverseBytes((int) ((freqCoeff) / freq));
    }


    public void pumpOn(boolean on) throws IOException {
        int trys=TRIES;
        while(trys>0)
        {
            sendcommand(CMD_PUMP_ON);
            writebyte((byte) (on==true?1:0));
            writechecksum();
            //readbyte(); //TODO: check queued
            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return;
            trys--;
        }
        logger.warn("CRC error");
    }

    public void valveOn(boolean on) throws IOException {
        int trys=TRIES;
        while(trys>0)
        {
            sendcommand(CMD_VALVE_ON);
            writebyte((byte) (on==true?1:0));
            writechecksum();
            //readbyte(); //TODO: check queued

            int[] crcword = readchecksumword();
            if (crcword[0] != 1)
                if ((crc & 0xFFFF) == (crcword[1] & 0xFFFF))
                    return;
            trys--;
        }
        logger.warn("CRC error");
    }

    public void reset() {
        try{
        port.close();}
        catch (Exception e){
            logger.warn("reset: close failed",e);
        }try {
            init(portName);
        }
        catch (Exception e)
        {
            logger.warn("reset: init failed",e);
        }

    }
}
