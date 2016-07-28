package org.omilab.omirob.dobot;

import java.nio.ByteBuffer;

/**
 * Created by Martin on 28.07.2016.
 */
public class TargetAnglePacket {
    float joint1Angle;
    float joint2Angle;
    float joint3Angle;
    float rHead;
    boolean isGrab;
    MovingMode movingMode;
    float gripperValue; /** Range: 90  to -90*/
    float pauseTimeSeconds;


    public static ByteBuffer write(ByteBuffer b, TargetAnglePacket tmp) {
        if (b == null)
            b = ByteBuffer.allocate(42);

        b.put((byte) 0xA5); //header
        b.put((byte) 6); //mode
        b.put((byte) 0);
        b.putFloat(tmp.joint1Angle);
        b.putFloat(tmp.joint2Angle);
        b.putFloat(tmp.joint3Angle);

        b.putFloat(tmp.rHead);
        b.putFloat(tmp.isGrab?1f:0f);
        b.putFloat(tmp.movingMode.getValue());
        b.putFloat(tmp.gripperValue);
        b.putFloat(tmp.pauseTimeSeconds);

        b.put((byte) 0xA5); //end
        return b;
    }
}
