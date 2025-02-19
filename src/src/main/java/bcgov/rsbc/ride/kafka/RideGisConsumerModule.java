package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.models.geolocationRecord;
import bcgov.rsbc.ride.kafka.service.ConsumerService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/consumegis")
public class RideGisConsumerModule {

    private final static Logger logger = LoggerFactory.getLogger(RideGisConsumerModule.class);

    @Inject
    ConsumerService consumerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String hello() {
        return "pong from GIS consumer";
    }

    @Incoming("incoming-gis-geolocation")
    @Blocking
    public void receiveGeolocation(Record<Long, geolocationRecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            geolocationRecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventToDecodedTopic(recordValue.toString(),"gis_geolocation",recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }
}