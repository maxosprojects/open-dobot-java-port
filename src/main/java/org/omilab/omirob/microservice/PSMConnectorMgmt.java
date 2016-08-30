package org.omilab.omirob.microservice;

import org.omilab.omirob.microservice.model.GenericRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/instanceMgmt")
public final class PSMConnectorMgmt {


	public PSMConnectorMgmt() {
	}

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public String manageInstance(final GenericRequest gr, final @Context HttpServletRequest servletRequest) {
		/*
		 * Dummy method, as this service does not need an instance concept. Instantiation and destruction of
		 * instances has no effects
		 */
		if(gr.getParams().get("mode").equals("create")) {
			return "1";
		}
		if(gr.getParams().get("mode").equals("delete")) {
			return "true";
		}
		return "";
	}

}
