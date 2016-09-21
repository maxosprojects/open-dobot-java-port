package org.omilab.omirob;

import freemarker.template.TemplateException;
import org.hashids.Hashids;
import org.omilab.omirob.opendobot.DobotSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.*;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
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
        HashMap vals=new HashMap();
        vals.put("staticpath",Settings.publicURL+"/static/");
        vals.put("streams", Settings.streams);
        vals.put("publicURL", Settings.publicURL);
        ByteArrayOutputStream bos = Freemarker.process(vals, "video");
        return Response.status(200).entity(bos.toString()).build();
    }

    @GET
    @Path("/auth")
    @Produces(MediaType.TEXT_HTML)
    public Response getAuth() throws IOException, TemplateException {
        Hashids hashids = new Hashids(Settings.salt);
        for(int i=0;i<48;i++){
            //long[] numbers = hashids.encode();
        }
        HashMap vals=new HashMap();
        vals.put("jsmpgpath",".");
        vals.put("streams", Settings.streams);
        vals.put("publicURL", Settings.publicURL);
        ByteArrayOutputStream bos = Freemarker.process(vals, "auth");
        return Response.status(200).entity(bos.toString()).build();
    }

    @POST
    @Path("/positionXYZ")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postPositionXYZ(String token, XYZParams params) {
        if(!checkToken(token)) {
            return Response.ok("").build();
        }
        else {
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            try {
                logger.info("postPositionXYZ: (" + params.x + ", " + params.y + ", " + params.z + ")");
                dobot.moveWithSpeed(params.x, params.y, params.z, speed, acc, 1000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Response.ok("").build();
        }
    }

    @GET
    @Path("/positionXYZ")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getPositionXYZ(String token, XYZParams params) {
        if(!checkToken(token)){
            XYZParams xyz=new XYZParams();
            return Response.ok(xyz).build();
        }
        else{
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            XYZParams xyz=new XYZParams();
            return Response.ok(xyz).build();
        }
    }

    @POST
    @Path("/sequence")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sequence(String token, String seq) {
        if(!checkToken(token)){
            return Response.ok("").build();
        }
        else {
            seq = seq.replace("\"", "");
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            try {
                String[] lines = seq.split("\\\\n");
                for (String line : lines) {
                    Scanner s = new Scanner(line);
                    String cmd = s.next().trim();
                    if (cmd.startsWith("#"))
                        continue;
                    if (cmd.equals("sleep"))
                        try {
                            Thread.sleep(Math.min(s.nextInt() * 1000, 10000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    if (cmd.equals("reset"))
                        dobot.reset();
                    else if (cmd.equals("move"))
                        dobot.moveWithSpeed(s.nextInt(), s.nextInt(), s.nextInt(), speed, acc, 1000);
                    else if (cmd.equals("pumpOn"))
                        dobot.pumpOn(s.nextBoolean());
                    else if (cmd.equals("valveOn"))
                        dobot.valveOn(s.nextBoolean());
                }
            } catch (IOException e) {
                logger.warn("Sequence error", e);
                e.printStackTrace();
            }

            System.out.println(seq);
            return Response.ok("").build();
        }
    }

    @POST
    @Path("/grabOn")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response grabOn(String token, boolean value) {
        if(!checkToken(token))
        {
            return Response.ok("").build();
        }else{
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
        try {
            dobot.pumpOn(value);
            dobot.valveOn(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok("").build();
        }
    }

    @POST
    @Path("/pumpOn")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pumpOn(String token, boolean value) {
        if(!checkToken(token)) {
            return Response.ok("").build();
        }
        else {
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            try {
                dobot.pumpOn(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Response.ok("").build();
        }
    }

    @POST
    @Path("/valveOn")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response valveOn(String token, boolean value) {
        if(!checkToken(token)) {
            return Response.ok("").build();
        }
        else {
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            try {
                dobot.valveOn(value);
                logger.info("Valve: " + value);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Response.ok("").build();
        }
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reset(String token) {
        if(!checkToken(token)) {
            return Response.ok("RESET").build();
        }
        else {
            DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");
            try {
                dobot.reset();
            } catch (IOException e) {
                logger.error("reset failed ", e);
            }
            return Response.ok("RESET").build();
        }
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

    private boolean checkToken(String token){
        return false;
    }
}