package bcgov.rsbc.ride.kafka;

import bcgov.rsbc.ride.kafka.models.*;
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


@Path("/consumedfv2")
public class RideDFv2ConsumerModule {

    private final static Logger logger = LoggerFactory.getLogger(RideDFv2ConsumerModule.class);

    @Inject
    ConsumerService consumerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String hello() {
        return "pong from df V2 consumer";
    }

    @Incoming("incoming-vievent")
    @Blocking
    public void receiveVi(Record<Long, viPayloadRecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            viPayloadRecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventToDecodedTopic(recordValue.toString(),recordValue.getEventType(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-twelvehrevent")
    @Blocking
    public void receiveTwelve(Record<Long, twelveHoursPayloadRecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            twelveHoursPayloadRecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventToDecodedTopic(recordValue.toString(),recordValue.getEventType(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-twentyfourhrevent")
    @Blocking
    public void receiveTwentyFour(Record<Long, twentyFourHoursPayloadRecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            twentyFourHoursPayloadRecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventToDecodedTopic(recordValue.toString(),recordValue.getEventType(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }
}