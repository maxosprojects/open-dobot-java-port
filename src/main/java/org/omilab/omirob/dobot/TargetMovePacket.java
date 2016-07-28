package org.omilab.omirob.dobot;

import java.nio.ByteBuffer;

/**
 * Created by Martin on 28.07.2016.
 */
public class TargetMovePacket {
    float x;
    float y;
    float z;
    float rHead;
    float isGrab;
    float movingMode;
    float gripperValue;
    float pauseTime;


    public static ByteBuffer write(ByteBuffer b, TargetMovePacket tmp) {
        if (b == null)
            b = ByteBuffer.allocate(42);

        b.put((byte) 0xA5); //header
        b.put((byte) 3); //mode
        b.put((byte) 0);
        b.putFloat(tmp.x);
        b.putFloat(tmp.y);
        b.putFloat(tmp.z);

        b.putFloat(tmp.rHead);
        b.putFloat(tmp.isGrab);
        b.putFloat(tmp.movingMode);

        b.putFloat(tmp.gripperValue);
        b.putFloat(tmp.pauseTime);

        b.put((byte) 0xA5); //end
        return b;
    }
}
