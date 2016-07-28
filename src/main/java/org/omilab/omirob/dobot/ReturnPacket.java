package org.omilab.omirob.dobot;

import java.nio.ByteBuffer;

/**
 * Created by Martin on 28.07.2016.
 */
public class ReturnPacket {
    float x;
    float y;
    float z;
    float rHead;
    float baseAngle;
    float longArmAngle;
    float shortArmAngle;
    float pawArmAngle;
    float isGrab;
    float gripperAngle;

    public static ReturnPacket parse(ByteBuffer b) {
        ReturnPacket r = new ReturnPacket();
        b.rewind();
        byte header = b.get(); //0xA5
        r.x = b.getFloat();
        r.y = b.getFloat();
        r.z = b.getFloat();
        r.rHead = b.getFloat();
        r.baseAngle = b.getFloat();
        r.longArmAngle = b.getFloat();
        r.shortArmAngle = b.getFloat();
        r.pawArmAngle = b.getFloat();
        r.isGrab = b.getFloat();
        r.gripperAngle = b.getFloat();
        byte end = b.get();
        return r;
    }
}
