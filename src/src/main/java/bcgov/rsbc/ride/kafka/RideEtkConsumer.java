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
import bcgov.rsbc.ride.kafka.models.evtpaymenteevent;
import bcgov.rsbc.ride.kafka.models.evtdisputeevent;
import bcgov.rsbc.ride.kafka.models.evtdisputeupdateevent;
import bcgov.rsbc.ride.kafka.models.evtpaymentqueryeevent;
import bcgov.rsbc.ride.kafka.models.evtcontraventionseevent;

import bcgov.rsbc.ride.kafka.service.etkConsumerService;



@Path("/consumeetk")
public class RideEtkConsumer {
    private final static Logger logger = LoggerFactory.getLogger(RideEtkConsumer.class);


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/ping")
    public String etkping() {
        return "pong from etk consumer";
    }


    // Monitors the incoming-issuance topic
    @Incoming("incoming-issuance")
    @Blocking
    public void receive(Record<Long, String> event) {
        try {
            String eventType="issuance";
            // Read the events from Kafka and send to the Process events method
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }

        } catch (Exception e) {
            logger.error("Exception occurred while reading decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payment")
    @Blocking
    public void receive_payment(Record<Long, String> event) {
        try {
            String eventType="payment";
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-dispute")
    @Blocking
    public void receive_dispute(Record<Long, String> event) {
        try {
            String eventType="dispute";
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-disputeupdate")
    @Blocking
    public void receive_disputeupdate(Record<Long, String> event) {
        try {
            String eventType="disputeupdate";
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-violations")
    @Blocking
    public void receive_violations(Record<Long, String> event) {
        try {
            String eventType="violations";
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }

    @Incoming("incoming-payquery")
    @Blocking
    public void receive_payquery(Record<Long, String> event) {
        try {
            String eventType="paymentquery";
            boolean eventStatus=etkConsumerService.processEtkEvents(event,eventType);
            if(eventStatus==false){
                throw new Exception("error in processing event");
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }


}