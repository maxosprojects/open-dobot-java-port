package org.omilab.omirob.microservice;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.omilab.omirob.Freemarker;
import org.omilab.omirob.Settings;
import org.omilab.omirob.microservice.model.GenericRequest;
import org.omilab.omirob.microservice.model.GenericServiceContent;
import org.omilab.omirob.slots.Slot;
import org.omilab.omirob.slots.SlotDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Path("/view")
public final class PSMConnectorView {
	private final static Logger logger = LoggerFactory.getLogger(PSMConnectorView.class);

	private static final int NUM_SLOTS = 48;

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
			return auth(servletRequest, request);
		}
		else if("control".equals(endpoint))
			return control(servletRequest, request);
		return new GenericServiceContent("unknown endpoint");
	}

	private GenericServiceContent control(HttpServletRequest servletRequest, GenericRequest request) {
		StringWriter output = new StringWriter();
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		try{
			cfg.setClassForTemplateLoading(this.getClass(), "/templates");
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			Template temp = cfg.getTemplate("control.ftl");
			Map<String, Object> data = new HashMap<>();
			data.put("jsmpgpath",Settings.publicURL + "/static/js/jsmpg.js");
			data.put("streams", Settings.streams);
			data.put("staticpath", Settings.publicURL + "/static/");

			data.put("publicURL", Settings.publicURL);
			temp.process(data, output);
		} catch(Exception e){
			e.printStackTrace();
			output.append("<div style=\"color: red;\">Problem with Template Engine</div>");
		}
		return new GenericServiceContent(output.toString());
	}

	private GenericServiceContent auth(HttpServletRequest servletRequest, GenericRequest request) {
		try{
			HashMap<Integer, Slot> slots = SlotDao.getSlots();
			synchronized (slots) {
				String button = request.getParams().get("action");
				String userName=request.getUsername().trim();
				HashMap vals = new HashMap();
				if (button != null
						&& button.length() > 0
						&& request.getUsername() != null
						&& !request.getUsername().equals("anonymousUser")) {
					vals.put("method","post");
					int slotNumber = Integer.parseInt(button);
					Slot slot = slots.get(slotNumber);

					final ZoneId zoneId = ZoneId.of("Europe/Vienna");
					final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId);
					int currentHour=zonedDateTime.getHour();
					int currentMinute=zonedDateTime.getMinute();
					if(currentHour*2+currentMinute/30>slotNumber){
						//Dont update past Slots
					}
					else if (slot == null) {
						slot = new Slot();
						slot.userName = request.getUsername();
						slot.which = slotNumber;
						Random random = new SecureRandom();
						byte[] bytes = new byte[10];
						random.nextBytes(bytes);
						slot.secret = Base64.getEncoder().encodeToString(bytes);
						slots.put(slotNumber, slot);
						SlotDao.save(slots);
					} else if (slot.userName.equals(request.getUsername())) {
						slots.remove(slotNumber);
						SlotDao.save(slots);
					}
				}
				String authToken="";
				for(Slot s: slots.values()){
					if(s.userName.equals(userName))
						authToken+=s.secret+",";
				}
				if(authToken.length()>0)
					authToken=authToken.substring(0,authToken.length()-1);


				HashMap s = new LinkedHashMap();
				for (int i = 0; i < NUM_SLOTS; i++) {
					s.put(i, slots.get(i));
				}
				vals.put("slots", s);
				vals.put("userName", request.getUsername());
				vals.put("staticpath", Settings.publicURL + "/static/");
				vals.put("publicURL", Settings.publicURL);
				vals.put("authToken", authToken);


				ByteArrayOutputStream bos = Freemarker.process(vals, "auth");
				return new GenericServiceContent(bos.toString());
			}
		} catch(Exception e){
			e.printStackTrace();
			return new GenericServiceContent("<div style=\"color: red;\">Problem with Template Engine</div>"+e.toString());
		}
	}
}