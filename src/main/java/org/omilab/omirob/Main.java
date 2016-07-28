package org.omilab.omirob;

import org.omilab.omirob.codegen.DispatchGen;
import org.omilab.omirob.codegen.HeaderGen;
import org.omilab.omirob.tinyrpc.TinyRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by Martin on 22.07.2016.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args){

        String header = HeaderGen.generate(IRobot.class);

        System.out.println(header);
        String dispatcher = DispatchGen.generate(IRobot.class);
        System.out.println(dispatcher);

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
