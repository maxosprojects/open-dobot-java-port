package org.omilab.omirob;

import org.omilab.omirob.slots.Slot;
import org.omilab.omirob.slots.SlotDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Created by martink82cs on 21.09.2016.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check if the HTTP Authorization header is present and formatted correctly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();
        try {
            // Validate the token
            validateToken(token);

        } catch (Exception e) {
            requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private void validateToken(String token) throws Exception {
        // Check if it was issued by the server and if it's not expired
        // Throw an Exception if the token is invalid
        final ZoneId zoneId = ZoneId.of("Europe/Vienna");
        final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId);
        zonedDateTime.getHour();
        HashMap<Integer, Slot> slots = SlotDao.getSlots();
        for(Slot s:slots.values()){
            int which=zonedDateTime.getHour()*2+zonedDateTime.getMinute()/30;
            if(s.which==which&&token.contains(s.secret)){  //TODO: split and use MessageDigest.isEqual()
                logger.info("VALID: "+ s.getUserName());
                return;
            }
        }
        throw new Exception("UNAUTHORIZED");
    }
}