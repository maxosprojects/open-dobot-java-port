package org.omilab.omirob;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.opendobot.DobotSDK;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerContainer;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.glassfish.jersey.servlet.ServletContainer;



/**
 * Created by Martin on 22.07.2016.
 */
public class OpenDobotMain {
    private final static Logger logger = LoggerFactory.getLogger(OpenDobotMain.class);



    public static void main(String[] args){

        String portname="COM22";
        try {
            //DobotSDK db = new DobotSDK(115200, portname, false, false, 1000);
            new Freemarker().init();
            //config.property("dobotSDK",db);
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
            server.join();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
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
