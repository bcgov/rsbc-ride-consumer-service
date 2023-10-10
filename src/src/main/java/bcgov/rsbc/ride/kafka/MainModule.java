package bcgov.rsbc.ride.kafka;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/ping")
public class MainModule {
    private final static Logger logger = LoggerFactory.getLogger(MainModule.class);
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public String pingservice() {
        return "pong from consumer service";
    }
}