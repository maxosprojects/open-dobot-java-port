package org.omilab.omirob;

import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;
import it.sauronsoftware.cron4j.Scheduler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.omilab.omirob.microservice.PSMConnectorAdmin;
import org.omilab.omirob.microservice.PSMConnectorMgmt;
import org.omilab.omirob.microservice.PSMConnectorView;
import org.omilab.omirob.opendobot.DobotSDK;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.omilab.omirob.slots.SlotDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.websocket.server.ServerContainer;
import java.io.IOException;
import java.util.EnumSet;

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
            config.register(AuthenticationFilter.class);
            //config.register(CORSFilter.class);
            config.property("dobotSDK",db);
            ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
            context.addServlet(jerseyServlet, "/*");
            server.setHandler(context);
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
            // Add WebSocket endpoint to javax.websocket layer
            server.start();
            server.dump(System.err);
            startScheduler();
            server.join();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

    private static void startScheduler(){
        Scheduler s = new Scheduler();
        s.schedule("* * * 0 0", () -> SlotDao.clear());
        s.start();
    }
}
