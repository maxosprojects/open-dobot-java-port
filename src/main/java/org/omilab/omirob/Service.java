package org.omilab.omirob;

import freemarker.template.TemplateException;
import org.omilab.omirob.opendobot.DobotSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

@Path("/")
public class Service {
    private final static Logger logger = LoggerFactory.getLogger(Service.class);
    @Context
    Configuration configuration;

    private static int speed = 50;
    private static int acc = 50;

    public Service() {

    }

    @GET
    public Response getIndex() throws IOException, TemplateException {
        ByteArrayOutputStream bos = Freemarker.process(new HashMap(), "video");
        return Response.status(200).entity(bos.toString()).build();
    }


    @POST
    @Path("/move")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response move(XYZParams params) {
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
            System.out.println(params.x + " " + params.y + " " + params.z);
            dobot.moveWithSpeed(params.x, params.y, params.z, speed, acc, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok("").build();
    }

    @POST
    @Path("/sequence")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response move(String seq) {
        seq=seq.replace("\"","");
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
        String[] lines = seq.split("\\\\n");
        for(String line:lines){
            Scanner s=new Scanner(line);
            String cmd=s.next().trim();
            if(cmd.startsWith("#"))
                continue;
            if(cmd.equals("sleep"))
                try {
                    Thread.sleep(Math.min(s.nextInt()*1000,10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            if(cmd.equals("reset"))
                dobot.reset();
            else if(cmd.equals("move"))
                    dobot.moveWithSpeed(s.nextInt(), s.nextInt(), s.nextInt(), speed, acc, 1000);
            else if(cmd.equals("pumpOn"))
                dobot.pumpOn(s.nextBoolean());
            else if(cmd.equals("valveOn"))
                dobot.valveOn(s.nextBoolean());
        }
        } catch (IOException e) {
            logger.warn("Sequence error",e);
            e.printStackTrace();
        }

        System.out.println(seq);
        return Response.ok("").build();
    }

    @POST
    @Path("/pumpOn")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pumpOn(boolean value) {
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
            dobot.pumpOn(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok("").build();
    }

    @POST
    @Path("/valveOn")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response valveOn(boolean value) {
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
            dobot.valveOn(value);
            logger.info("Valve: "+value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok("").build();
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset() {
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
            dobot.reset();
        } catch (IOException e) {
            logger.error("reset failed ",e);
        }
        return Response.ok("RESET").build();
    }

    @GET
    @Path("/static/{filename:.*}")
    public Response getStaticFile(@PathParam("filename") String filename) {
        try {
            InputStream s = Service.class.getResourceAsStream("/static/" + filename);
            return Response.status(200).entity(s).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.toString()).build();
        }
    }
}