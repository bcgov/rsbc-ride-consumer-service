package bcgov.rsbc.ride.kafka.service;

import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.HashEntry;
import scala.collection.mutable.HashTable;

import java.time.Duration;
import java.util.Dictionary;
import java.util.HashMap;

@ApplicationScoped
public class DFv2ConsumerService {

    private final static Logger logger = LoggerFactory.getLogger(DFv2ConsumerService.class);

    @Inject
    @Channel("outgoing-vievent-decoded")
    MutinyEmitter<Record<String, String>> emitterViEvent;

    @Inject
    @Channel("outgoing-twelvehrevent-decoded")
    MutinyEmitter<Record<String, String>> emitterTwelveHourEvent;

    @Inject
    @Channel("outgoing-twentyfourhrevent-decoded")
    MutinyEmitter<Record<String, String>> emitterTwentyFourHourEvent;

    private final HashMap<String, MutinyEmitter<Record<String, String>>> emitterMap = new HashMap<>();

    private void initializeMap() {
        emitterMap.put("vi_submitted", emitterViEvent);
        emitterMap.put("12hr_submitted", emitterTwelveHourEvent);
        emitterMap.put("24hr_submitted", emitterTwentyFourHourEvent);
    }

    public void publishEventToDecodedTopic(String eventPayload,String eventType,Long uid) {
        logger.info("publishing event id {} type {} to decoded topic", uid, eventType);
        logger.debug("payload: {}", eventPayload);
        initializeMap();
        try {
            if (emitterMap.containsKey(eventType)) {
                logger.debug(emitterMap.get(eventType).toString());
                emitterMap.get(eventType).send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
            } else {
                logger.error("no matches for the event type: {}", eventType);
            }
        } catch (Exception e) {
            logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
        }
    }
}