package org.omilab.omirob.microservice;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.omilab.omirob.microservice.model.GenericRequest;
import org.omilab.omirob.microservice.model.GenericServiceContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Path("/admin")
public final class PSMConnectorAdmin {

	private static final Logger logger = LoggerFactory.getLogger(PSMConnectorAdmin.class);

	public PSMConnectorAdmin() {
	}

	@POST
	@Path("/{instanceid}/{endpoint}")
	@Produces("application/json")
	public GenericServiceContent processAdmin(final GenericRequest request,
											  final @PathParam("instanceid") Long instanceid,
											  final @PathParam("endpoint") String endpoint,
											  final @Context HttpServletRequest servletRequest) {

		/*
		 * Example on how to supply functionality for the protected admin area.
		 * Example access on the request object, but no use of template engine
		 */

		return new GenericServiceContent("<div style=\"color:white;\">Please implement me, "+ request.getUsername() +"!</div>");
	}

}
