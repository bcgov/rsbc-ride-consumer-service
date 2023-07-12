package bcgov.rsbc.ride.kafka;

import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.kafka.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Set;


import bcgov.rsbc.ride.kafka.models.evtissuanceeevent;




@Path("/consumeetk")
public class RideEtkConsumer {
    private final static Logger logger = LoggerFactory.getLogger(RideEtkConsumer.class);


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String etkping() {
        return "pong from etk consumer";
    }


    @Incoming("incoming-issuance")
    @Blocking
//    public void receive(String event) {
    public void receive(Record<Long, String> event) {
        try {
            logger.info("Payload: {}", event);
            logger.info("Type of event: {}", event.getClass());
            String recordKey= String.valueOf(event.key());
            String recordValue = event.value();
//            String eventId = event.substring(0, 4);
//            logger.info("Event ID: {}", eventId);
            String eventString = recordValue.substring(5);
//            logger.info("Payload: {}", eventString);
//            ObjectMapper objectMapper = new ObjectMapper();
//            evtissuanceeevent evt = objectMapper.readValue(eventString, evtissuanceeevent.class);
//            logger.info("Payload: {}", evt.getTicketNumber());
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded issuance event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payment")
    @Blocking
    public void receive_payment(String event) {
        logger.info("Payload: {}", event);
        try {
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded payment event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-dispute")
    @Blocking
    public void receive_dispute(String event) {
        logger.info("Payload: {}", event);
        try {
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded dispute event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-disputeupdate")
    @Blocking
    public void receive_disputeupdate(String event) {
        logger.info("Payload: {}", event);
        try {
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded disputeupdate event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-violations")
    @Blocking
    public void receive_violations(String event) {
        logger.info("Payload: {}", event);
        try {
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded violations event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payquery")
    @Blocking
    public void receive_payquery(String event) {
        logger.info("Payload: {}", event);
        try {
            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded payquery event UID: {}", uid);
//            consumerService.publishEventtoIssuanceDecodedTopic(event);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }


}
