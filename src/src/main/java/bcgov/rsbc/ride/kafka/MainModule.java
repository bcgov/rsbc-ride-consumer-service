package bcgov.rsbc.ride.kafka;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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