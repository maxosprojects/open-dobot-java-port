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

@Path("/")
public class Service {
    private final static Logger logger = LoggerFactory.getLogger(Service.class);
    @Context
    Configuration configuration;

    int speed=50;
    int acc=50;

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
    public Response move(@FormParam("x") int x,
                              @FormParam("y") int y,
                              @FormParam("z") int z) {
        DobotSDK dobot = (DobotSDK) configuration.getProperty("dobotSDK");

        try {
            System.out.println(x +" " +y+" "+z);
            dobot.moveWithSpeed(x,y,z,speed,acc,1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Response.ok("").build();
    }

    @GET
    @Path("/static/{filename:.*}")
    public Response getStaticFile(@PathParam("filename") String filename) {
        try{
            InputStream s = Service.class.getResourceAsStream("/static/" + filename);
            return Response.status(200).entity(s).build();
        }
        catch (Exception e){
            return Response.serverError().entity(e.toString()).build();
        }
    }

}