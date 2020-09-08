package org.neworbit.template;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger; 

@Path("/welcome")
public class WelcomeResource {

    Logger logger = Logger.getLogger(this.getClass());

    @ConfigProperty(name = "welcome.message")
    String welcomeMessage;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String welcome() {
        logger.info("welcome method executing");
        return welcomeMessage;
    }
}