package org.omilab.omirob;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.omilab.omirob.opendobot.DobotSDK;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.omilab.omirob.streaming.FFMpegThread;
import org.omilab.omirob.streaming.ServiceStream;
import org.omilab.omirob.streaming.ServiceWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.websocket.server.ServerContainer;
import java.io.IOException;

import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){
        try {
            DobotSDK db=null;
            try {
                db = new DobotSDK(115200, Settings.portName, false, false, 1000);
            }
            catch (Exception e){
                logger.warn("Dobot init failed",e);
            }

            new Freemarker().init();
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(8080);
            server.addConnector(connector);

            // Setup the basic application "context" for this application at "/"
            // This is also known as the handler tree (in jetty speak)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            ResourceConfig config = new ResourceConfig();
            config.register(Service.class);
            config.property("dobotSDK",db);
            config.register(JacksonFeature.class);
            ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
            context.addServlet(jerseyServlet, "/*");
            context.addServlet(ServiceStream.class, "/stream/input");
            server.setHandler(context);


            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint to javax.websocket layer
            wscontainer.addEndpoint(ServiceWS.class);
            server.start();
            server.dump(System.err);
            try{
            Thread t=new Thread(new FFMpegThread());
            //t.start();
            }catch (Exception e){
                logger.warn("Failed to start ffmpeg");
            }
            //moveTestXYZ(portname);
            server.join();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    private static void moveTestXYZ(String portname) {
        try {
            DobotSDK db = new DobotSDK(115200, portname, false, false, 1000);

            int speed=50;
            int acc=50;
            while(true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                db.valveOn(true);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                db.valveOn(false);

//                db.moveWithSpeed(150f, -50, 100f, speed, acc, 1000);
//
//                db.moveWithSpeed(150f, 50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(300f, 50, 100f, speed, acc, 1000);
//                db.pumpOn(true);
//                db.moveWithSpeed(300f, 50, 150f, speed, acc, 1000);
//                db.pumpOn(false);
//                db.moveWithSpeed(300f, 50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(300f, -50, 100f, speed, acc, 1000);
//                db.moveWithSpeed(150f, -50, 100f, speed, acc, 1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
