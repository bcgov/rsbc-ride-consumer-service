package bcgov.rsbc.ride.kafka;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import io.smallrye.reactive.messaging.kafka.Record;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
//import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.Record;
import io.smallrye.reactive.messaging.annotations.Blocking;

import bcgov.rsbc.ride.kafka.models.testevent;
import bcgov.rsbc.ride.kafka.models.payloadrecord;

import bcgov.rsbc.ride.kafka.models.appacceptedevent;
import bcgov.rsbc.ride.kafka.models.appacceptedpayloadrecord;
import bcgov.rsbc.ride.kafka.models.disclosuresentevent;
import bcgov.rsbc.ride.kafka.models.disclosuresentpayloadrecord;
import bcgov.rsbc.ride.kafka.models.evidencesubmittedevent;
import bcgov.rsbc.ride.kafka.models.evidencesubmittedpayloadrecord;
import bcgov.rsbc.ride.kafka.models.payrecvdevent;
import bcgov.rsbc.ride.kafka.models.payrecvdpayloadrecord;
import bcgov.rsbc.ride.kafka.models.reviewscheduleddevent;
import bcgov.rsbc.ride.kafka.models.reviewscheduledpayloadrecord;

import bcgov.rsbc.ride.kafka.service.ConsumerService;


@Path("/consumedf")
public class RideDFConsumerModule {

    private final static Logger logger = LoggerFactory.getLogger(RideConsumerModule.class);

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
//    public void receive(appacceptedpayloadrecord event) {
    public void receive(Record<Long, appacceptedpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            appacceptedpayloadrecord recordValue = event.value();

//            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded event UID: {}", recordKey);
//            logger.info("Kafka decoded event UID: {}", recordValue.toString());
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-disclosuresent")
    @Blocking
//    public void receive(disclosuresentpayloadrecord event) {
    public void receive_disclosure(Record<Long, disclosuresentpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            disclosuresentpayloadrecord recordValue = event.value();
//            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-evidencesubmit")
    @Blocking
//    public void receive(evidencesubmittedpayloadrecord event) {
    public void receive_evsubmitted(Record<Long, evidencesubmittedpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            evidencesubmittedpayloadrecord recordValue = event.value();
//            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payreceived")
    @Blocking
//    public void receive(payrecvdpayloadrecord event) {
    public void receive_payrecvd(Record<Long, payrecvdpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            payrecvdpayloadrecord recordValue = event.value();
//            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-reviewscheduled")
    @Blocking
//    public void receive(reviewscheduledpayloadrecord event) {
    public void receive_revsched(Record<Long, reviewscheduledpayloadrecord> event) {
        logger.info("Payload: {}", event);
        try {
            Long recordKey= event.key();
            reviewscheduledpayloadrecord recordValue = event.value();
//            Long uid = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            logger.info("Kafka decoded event UID: {}", recordKey);
            consumerService.publishEventtoDecodedTopic(recordValue.toString(),recordValue.getEventType().toString(),recordKey);
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }


}