package org.omilab.omirob;

import org.omilab.omirob.codegen.TinyRPCMethod;

/**
 * Created by Martin on 22.07.2016.
 */
public interface IRobot {
    @TinyRPCMethod(id=1)
    public void driveStraight(int speedPercent);

    @TinyRPCMethod(id=2)
    public void rotate(int speedPercent);

    @TinyRPCMethod(id=3)
    public int getLightIntensity();

    @TinyRPCMethod(id=4)
    public int getLightRGB();

    @TinyRPCMethod(id=5)
    public int getSonarMm();

}
