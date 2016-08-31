package org.omilab.omirob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Martin on 25.08.2016.
 */
public class Settings {
    private final static Logger logger = LoggerFactory.getLogger(Settings.class);
    public static String portName;
    public static int port;
    public static int width;
    public static int height;
    public static String ffmpegCmd;

    static {
        read();
    }

    private Settings(){
    }

    private static void read() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
            prop.load(input);
            portName=prop.getProperty("portName");
            port=Integer.parseInt(prop.getProperty("port"));
            width=Integer.parseInt(prop.getProperty("width"));
            height=Integer.parseInt(prop.getProperty("height"));
            ffmpegCmd=prop.getProperty("ffmpegCmd");
        } catch (IOException ex) {
            logger.error("Cant load config", ex);
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}