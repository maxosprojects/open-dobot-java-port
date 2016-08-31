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
import org.omilab.omirob.microservice.PSMConnectorAdmin;
import org.omilab.omirob.microservice.PSMConnectorMgmt;
import org.omilab.omirob.microservice.PSMConnectorView;
import org.omilab.omirob.opendobot.DobotSDK;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.omilab.omirob.streaming.FFMpegThread;
import org.omilab.omirob.streaming.ServiceStream;
import org.omilab.omirob.streaming.ServiceWS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.websocket.server.ServerContainer;
import java.io.IOException;
import org.glassfish.jersey.jackson.JacksonFeature;

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
            //config.property("dobotSDK",db);
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Settings.port);
            server.addConnector(connector);

            // Setup the basic application "context" for this application at "/"
            // This is also known as the handler tree (in jetty speak)
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            ResourceConfig config = new ResourceConfig();
            config.register(Service.class);
            config.register(PSMConnectorAdmin.class);
            config.register(PSMConnectorMgmt.class);
            config.register(PSMConnectorView.class);
            config.register(JacksonFeature.class);
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
            if(Settings.ffmpegCmd.trim().length()>0)
                try{
                    Thread t=new Thread(new FFMpegThread(Settings.ffmpegCmd));
                    t.start();
                }catch (Exception e){
                    logger.warn("Failed to start ffmpeg");
                }
            else
                logger.info("ffmpegCmd empty, not starting ffmpeg");

            server.join();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}
