package org.omilab.omirob.opendobot;

/**
 * Created by Martin on 04.08.2016.
 */
public class CmdVal {
    public int cmd;
    public int steps;
    public float leftOver;

    public CmdVal(int cmd, int steps, float leftOver) {
        this.cmd = cmd;
        this.steps = steps;
        this.leftOver = leftOver;
    }
}
