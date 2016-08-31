package org.omilab.omirob.microservice;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.omilab.omirob.microservice.model.GenericServiceContent;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/view")
public final class PSMConnectorView {

	private final static String OUTSIDE_URL = "http://localhost:8181";

	public PSMConnectorView() {
	}

	@POST
	@Path("/{instanceid}/{endpoint}")
	@Produces("application/json")
	@Consumes("application/json")
	public GenericServiceContent processRequest(final @PathParam("instanceid") Long instanceid,
												final @PathParam("endpoint") String endpoint,
												final @Context HttpServletRequest servletRequest) {




		StringWriter output = new StringWriter();
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		//String[] appUrl = this.env.getProperty("app.url").split("/");
		//String webAppPath = System.getProperty("user.dir") + "/webapps/" + appUrl[appUrl.length-1];
		//String webAppPath = "/home/david/dev/server/tomcat7/webapps/globalnetworkservice";
		//String staticPath = webAppPath + "/WEB-INF/classes/static";
		//String templatePath = staticPath + "/templates";
		try{
			cfg.setClassForTemplateLoading(this.getClass(), "/templates");
		//	cfg.setDirectoryForTemplateLoading(new File(templatePath));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

			Template temp = cfg.getTemplate("video.ftl");

			Map<String, Object> data = new HashMap<>();
			data.put("sidebarcss",OUTSIDE_URL + "/static/css/simple-sidebar.css");
			data.put("jsmpgpath",OUTSIDE_URL + "/static/js/jsmpg.js");
			temp.process(data, output);
		} catch(Exception e){
			e.printStackTrace();
			output.append("<div style=\"color: red;\">Problem with Template Engine</div>");
		}

		return new GenericServiceContent(output.toString());
	}

}