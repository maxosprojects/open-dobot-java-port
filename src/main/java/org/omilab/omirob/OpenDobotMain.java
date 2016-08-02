package org.omilab.omirob;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.dobot.TargetMovePacket;
import org.omilab.omirob.opendobot.OpenDobotDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Enumeration;

/**
 * Created by Martin on 22.07.2016.
 */
public class OpenDobotMain {
    private final static Logger logger = LoggerFactory.getLogger(OpenDobotMain.class);




    public static void main(String[] args){

        new Freemarker().init();
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(9998).build();
        ResourceConfig config = new ResourceConfig(Service.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);


        String portname="COM22";
        OpenDobotDriver dobot = new OpenDobotDriver(portname);



    }
}
