package org.omilab.omirob.opendobot;

import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Enumeration;

/**
 * Created by Martin on 02.08.2016.
 */
public class OpenDobotDriver {
    private final int toolRotation=0;
    private final int gripper=480;
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


    private byte readbyte() throws IOException {
        int data=in.read();
        if(data<0)
                throw new RuntimeException("readbyte()");
        return (byte) (data&0xFF);
    }

    private void sendcommand(byte command) throws IOException {
            crc_clear();
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
        return null;
    }

//    private byte[] writeread(byte cmd, int length) throws IOException {
//        int trys=10;
//        while (trys>0) {
//            sendcommand(cmd);
//            writechecksum();
//            out.flush();
//            readbyte()
//            byte[] ret=new byte[length];
//            for (int i = 0; i < length; i++) {
//                int val = in.read();
//                ret[i] = (byte) (val & 0xFF);
//                crc_update(ret[i]);
//            }
//            int[] crcword = readchecksumword();
//            if (crcword[0]!=1)
//                if((crc&0xFFFF) == (crcword[1]&0xFFFF))
//                    return ret;
//            trys -= 1;
//        }
//        return null;
//    }

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
        out.write((crc>>8)&0xFF);
        out.write(crc&0xFF);
    }



/*
    private void write(int cmd,List<> write_commands=list()) {
        int trys = 10;
		while (trys>0) {
            sendcommand(cmd)

            for c in write_commands:
    c[0](c[1])

            self._writechecksum()
            self._port.send()
    crc = self._readchecksumword()
            if crc[0]:
            if (crc&0xFFFF == (crc[1]&0xFFFF):
            return true;
            trys -= 1;
        }
            return false;

    }

*/


    public OpenDobotDriver(String portname){
        CommPortIdentifier portid = null;
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            portid = (CommPortIdentifier) e.nextElement();
            System.out.println("found " + portid.getName());
            if(portid.getName().equalsIgnoreCase(portname))
                break;
        }
        try {
            SerialPort port = (SerialPort) portid.open("asdf", 1000);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            port.enableReceiveTimeout(2000);
            out = port.getOutputStream();
            in = port.getInputStream();

            port.setDTR(true);
            port.setRTS(true);
            Thread.sleep(1000);
            boardVersion();

        } catch (PortInUseException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (UnsupportedCommOperationException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }


    }
}
