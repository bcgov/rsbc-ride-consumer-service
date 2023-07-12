package bcgov.rsbc.ride.kafka.service;

import bcgov.rsbc.ride.kafka.models.evtissuanceeevent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import bcgov.rsbc.ride.kafka.models.evtissuanceeevent;
import bcgov.rsbc.ride.kafka.models.evtpaymenteevent;
import bcgov.rsbc.ride.kafka.models.evtdisputeevent;
import bcgov.rsbc.ride.kafka.models.evtdisputeupdateevent;
import bcgov.rsbc.ride.kafka.models.evtpaymentqueryeevent;
import bcgov.rsbc.ride.kafka.models.evtcontraventionseevent;

@ApplicationScoped
public class etkConsumerService {
    private final static Logger logger = LoggerFactory.getLogger(etkConsumerService.class);



    // Method to process the events received from Kafka
    public static boolean processEtkEvents(Record<Long, String> event,String eventType) {
        boolean eventStatus = false;
        try {
            logger.debug("Payload: {}", event);
            logger.debug("Type of event: {}", event.getClass());
            // Read the event key which is the event id in Kafka
            String recordKey= String.valueOf(event.key());
            // Read the event value which is the event payload in Kafka
            String recordValue = event.value();
            // Kafka event payload has a prefix of "data:" which needs to be removed
            String eventString = recordValue.substring(5);
            ObjectMapper objectMapper = new ObjectMapper();
            switch(eventType) {
                // Switch case to handle different event types
                // For each event type there is a model class which is used to deserialize the event payload                   
                // The deserialized event payload is logged. evt to be used for further business logic processing             
                case "issuance":
                    evtissuanceeevent evt = objectMapper.readValue(eventString, evtissuanceeevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt
                    logger.info("Payload: {}", evt);
                    logger.info("Kafka decoded issuance event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                case "payment":
                    evtpaymenteevent evt1 = objectMapper.readValue(eventString, evtpaymenteevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt1
                    logger.info("Payload: {}", evt1);
                    logger.info("Kafka decoded payment event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                case "dispute":
                    evtdisputeevent evt2 = objectMapper.readValue(eventString, evtdisputeevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt2
                    logger.info("Payload: {}", evt2);
                    logger.info("Kafka decoded dispute event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                case "disputeupdate":
                    evtdisputeupdateevent evt3 = objectMapper.readValue(eventString, evtdisputeupdateevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt3
                    logger.info("Payload: {}", evt3);
                    logger.info("Kafka decoded disputeupdate event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                case "paymentquery":
                    evtpaymentqueryeevent evt4 = objectMapper.readValue(eventString, evtpaymentqueryeevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt4
                    logger.info("Payload: {}", evt4);
                    logger.info("Kafka decoded paymentquery event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                case "violations":
                    evtcontraventionseevent evt5 = objectMapper.readValue(eventString, evtcontraventionseevent.class);
                    // TODO: Add business logic processing here. The event payload is available in evt5
                    logger.info("Payload: {}", evt5);
                    logger.info("Kafka decoded violations event UID: {}", recordKey);
                    eventStatus=true;
                    break;
                default:
                    logger.error("Unknown event type: {}", eventType);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Exception occurred while reading decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
        return eventStatus;
    }
}
