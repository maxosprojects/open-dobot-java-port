package org.omilab.omirob.opendobot;

import java.io.IOException;

/**
 * Created by martin on 30.08.2016.
 */
public interface IDobotSDK {
    void InitializeAccelerometers() throws IOException;

    void moveWithSpeed(float xx, float yy, float zz, float maxVel, float accel, float toolRotation) throws IOException;

    void pumpOn(boolean on) throws IOException;

    void valveOn(boolean on) throws IOException;

    void reset() throws IOException;
}
