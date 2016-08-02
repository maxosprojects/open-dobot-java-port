package org.omilab.omirob;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.omilab.omirob.dobot.TargetMovePacket;
import org.omilab.omirob.tinyrpc.TinyRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Enumeration;

/**
 * Created by Martin on 22.07.2016.
 */
public class DobotMain {
    private final static Logger logger = LoggerFactory.getLogger(DobotMain.class);

    public static void main(String[] args){

        new Freemarker().init();
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(9998).build();
        ResourceConfig config = new ResourceConfig(Service.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);


        String portname="COM22";
        CommPortIdentifier portid = null;
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            portid = (CommPortIdentifier) e.nextElement();
            System.out.println("found " + portid.getName());
            if(portid.getName().equalsIgnoreCase(portname))
                break;
        }
        try {
            SerialPort port = (SerialPort) portid.open("asdf", 1000);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            TargetMovePacket tmp=new TargetMovePacket();
            tmp.x=0;
            tmp.y=0;
            tmp.z=0;
            tmp.isGrab=1;
            OutputStream s = port.getOutputStream();
            s.write(TargetMovePacket.write(ByteBuffer.allocate(42), tmp).array());
        } catch (PortInUseException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (UnsupportedCommOperationException e1) {
            e1.printStackTrace();
        }

    }
}
