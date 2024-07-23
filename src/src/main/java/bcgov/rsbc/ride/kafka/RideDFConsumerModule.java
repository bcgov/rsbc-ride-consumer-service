package bcgov.rsbc.ride.kafka;

import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.reactive.messaging.annotations.Blocking;
import bcgov.rsbc.ride.kafka.models.appacceptedpayloadrecord;
import bcgov.rsbc.ride.kafka.models.disclosuresentpayloadrecord;
import bcgov.rsbc.ride.kafka.models.evidencesubmittedpayloadrecord;
import bcgov.rsbc.ride.kafka.models.payrecvdpayloadrecord;
import bcgov.rsbc.ride.kafka.models.reviewscheduledpayloadrecord;
import bcgov.rsbc.ride.kafka.service.ConsumerService;


@Path("/consumedf")
public class RideDFConsumerModule {

    private final static Logger logger = LoggerFactory.getLogger(RideDFConsumerModule.class);

    @Inject
    ConsumerService consumerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String hello() {
        return "pong from df consumer";

    }

    @Incoming("incoming-appaccepted")
    @Blocking
    public void receive(Record<Long, appacceptedpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            appacceptedpayloadrecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-disclosuresent")
    @Blocking
    public void receive_disclosure(Record<Long, disclosuresentpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            disclosuresentpayloadrecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-evidencesubmit")
    @Blocking
    public void receive_evsubmitted(Record<Long, evidencesubmittedpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            evidencesubmittedpayloadrecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payreceived")
    @Blocking
    public void receive_payrecvd(Record<Long, payrecvdpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            payrecvdpayloadrecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-reviewscheduled")
    @Blocking
    public void receive_revsched(Record<Long, reviewscheduledpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            reviewscheduledpayloadrecord recordValue = event.value();
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }


}