package org.omilab.omirob;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.codegen.DispatchGen;
import org.omilab.omirob.codegen.HeaderGen;
import org.omilab.omirob.tinyrpc.TinyRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){

        new Freemarker().init();
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(9998).build();
        ResourceConfig config = new ResourceConfig(Service.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);


        String portname="COM19";
        CommPortIdentifier portid = null;
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            portid = (CommPortIdentifier) e.nextElement();
            System.out.println("found " + portid.getName());
            if(portid.getName().equalsIgnoreCase(portname))
                break;
        }

        try {
            IRobot handler = (IRobot) TinyRpcHandler.createHandler(IRobot.class, portid);
            handler.driveStraight(10);
            handler.getLightIntensity();
            handler.rotate(0xDEADBEEF);
        } catch (PortInUseException e1) {
            logger.error("",e1);
        } catch (IOException e1) {
            logger.error("",e1);
        } catch (UnsupportedCommOperationException e1) {
            logger.error("",e1);
        }
    }
}
