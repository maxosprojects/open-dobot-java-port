package org.omilab.omirob;

import freemarker.template.TemplateException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Path("/")
public class Service {
    @GET
    public Response getIndex() throws IOException, TemplateException {
        ByteArrayOutputStream bos = Freemarker.process(new HashMap(), "index");
        return Response.status(200).entity(bos.toString()).build();
    }

//    @GET
//    @Path("/{param}")
//    public Response getMsg(@PathParam("param") String msg) {
//        String output = "Jersey say : " + msg;
//        return Response.status(200).entity(output).build();
//    }

    @GET
    @Path("/static/{filename}")
    public Response getStaticFile(@PathParam("filename") String filename) {
        File file = new File(Service.class.getResource("/static/"+filename).getFile());
        return Response.status(200).entity(file).build();
    }

}