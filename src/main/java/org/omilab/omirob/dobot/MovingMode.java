package org.omilab.omirob.dobot;

/**
 * Created by Martin on 28.07.2016.
 */
public enum MovingMode {
    JUMP(0f),
    MOVL(1f),
    MOVJ(2f);
    private float value;

    MovingMode(float f) {
        this.value=f;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
