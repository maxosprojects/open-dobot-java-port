package org.omilab.omirob;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.opendobot.DobotSDK;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;


/**
 * Created by Martin on 22.07.2016.
 */
public class OpenDobotMain {
    private final static Logger logger = LoggerFactory.getLogger(OpenDobotMain.class);

    public static void main(String[] args){

        String portname="COM22";
        try {
            DobotSDK db = new DobotSDK(115200, portname, false, false, 1000);
            new Freemarker().init();
            URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(9998).build();
            ResourceConfig config = new ResourceConfig(Service.class);
            config.property("dobotSDK",db);
            HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        } catch (IOException e) {
            e.printStackTrace();
        }




        //moveTest(portname);

//        try {
//            DobotSDK db = new DobotSDK(115200, portname, false, false, 1000);
//
//            int speed=50;
//            int acc=50;
//            while(true) {
//                db.moveWithSpeed(150f, -50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(150f, 50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(300f, 50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(300f, -50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(150f, -50, 100f, speed, acc, 1000);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    private static void moveTest(String portname){
        OpenDobotDriver dobot = new OpenDobotDriver(portname);
        try {
            for(int i=0;i<200;i++) {
                while(true) {
                    byte res = dobot.steps(OpenDobotDriver.stepsToCmdVal(10),
                            OpenDobotDriver.stepsToCmdVal(10),
                            OpenDobotDriver.stepsToCmdVal(10)
                            , 0, 0, 0, (short) 1000, (short) 1000);
                    if(res==1)
                        break;
                }
            }
            for(int i=0;i<200;i++) {
                while(true) {
                    byte res = dobot.steps(OpenDobotDriver.stepsToCmdVal(10),
                            OpenDobotDriver.stepsToCmdVal(10),
                            OpenDobotDriver.stepsToCmdVal(10)
                            , 1, 1, 1, (short) 1000, (short) 1000);
                    if(res==1)
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
