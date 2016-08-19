package org.omilab.omirob.microservice;

import org.omilab.omirob.microservice.model.GenericServiceContent;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/view")
public final class PSMConnectorView {


	public PSMConnectorView() {
	}

	@POST
	@Path("/{instanceid}/{endpoint}")
	@Produces("application/json")
	@Consumes("application/json")
	public GenericServiceContent processRequest(final @PathParam("instanceid") Long instanceid,
												final @PathParam("endpoint") String endpoint,
												final @Context HttpServletRequest servletRequest) {



		return new GenericServiceContent("hello!");
	}

}