package org.omilab.omirob;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.opendobot.DobotSDK;
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

        new Freemarker().init();
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(9998).build();
        ResourceConfig config = new ResourceConfig(Service.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        //String portname="COM22";
        //OpenDobotDriver dobot = new OpenDobotDriver(portname);
        try {
            DobotSDK db = new DobotSDK(115200, "COM22", false, false, 1000);
            db.moveWithSpeed(0, 0, 0, 10, 5, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
