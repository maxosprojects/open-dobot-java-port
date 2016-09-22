package org.omilab.omirob.slots;

/**
 * Created by martink82cs on 16.09.2016.
 */
public class Slot {
    public int which;
    public String userName;
    public String secret;

    public String getUserName() {
        return userName;
    }

    public int getWhich() {
        return which;
    }

    public String getSecret() {
        return secret;
    }
}
