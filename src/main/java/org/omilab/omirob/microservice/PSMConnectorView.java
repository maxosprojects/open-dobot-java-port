package org.omilab.omirob.microservice;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.omilab.omirob.Settings;
import org.omilab.omirob.StreamInfo;
import org.omilab.omirob.microservice.model.GenericRequest;
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

	public PSMConnectorView() {
	}

	@POST
	@Path("/{instanceid}/ajax")
	public String processRequest(String x) {
		return  x;
	}

	@POST
	@Path("/{instanceid}/{endpoint}")
	@Produces("application/json")
	@Consumes("application/json")
	public GenericServiceContent processRequest(final GenericRequest request,
												final @PathParam("instanceid") Long instanceid,
												final @PathParam("endpoint") String endpoint,
												final @Context HttpServletRequest servletRequest) {
		if("auth".equals(endpoint)){
			return auth(servletRequest);
		}
		else if("demo".equals(endpoint))
			return demo(servletRequest);

		return new GenericServiceContent("unknown endpoint");
	}

	private GenericServiceContent demo(HttpServletRequest servletRequest) {
		StringWriter output = new StringWriter();
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		try{
			cfg.setClassForTemplateLoading(this.getClass(), "/templates");
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			Template temp = cfg.getTemplate("video.ftl");
			Map<String, Object> data = new HashMap<>();
			data.put("jsmpgpath",Settings.publicURL + "/static/js/jsmpg.js");
			data.put("streams", Settings.streams);
			data.put("publicURL", Settings.publicURL);
			temp.process(data, output);
		} catch(Exception e){
			e.printStackTrace();
			output.append("<div style=\"color: red;\">Problem with Template Engine</div>");
		}
		return new GenericServiceContent(output.toString());
	}

	private GenericServiceContent auth(HttpServletRequest servletRequest) {

		return new GenericServiceContent("auth");

	}

}