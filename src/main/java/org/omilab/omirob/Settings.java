package org.omilab.omirob;
import com.sun.org.apache.xerces.internal.parsers.IntegratedParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
    public static StreamInfo[] streams;
    public static String publicURL;
    public static String salt;
    public static float baseCalibration;


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
            portName=prop.getProperty("portName","ttyUSB1");
            port=Integer.parseInt(prop.getProperty("port", "8080"));
            baseCalibration=Float.parseFloat(prop.getProperty("baseCalibration","1.48"));

            List<StreamInfo> streamList=new ArrayList<StreamInfo>();
            for(int i=0;true;i++){
                StreamInfo si=new StreamInfo();
                si.url=prop.getProperty("stream"+(i+1)+".url");
                if(si.url==null)
                    break;
                si.width=Integer.parseInt(prop.getProperty("stream"+(i+1)+".width", "640"));
                si.height=Integer.parseInt(prop.getProperty("stream"+(i+1)+".height", "360"));
                streamList.add(si);
            }
            streams=streamList.toArray(new StreamInfo[0]);
            publicURL=prop.getProperty("publicURL", "127.0.0.1");
            salt=prop.getProperty("salt", "GOIHFVBQEF");


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