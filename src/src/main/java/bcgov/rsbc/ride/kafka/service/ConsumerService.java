package bcgov.rsbc.ride.kafka.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.microprofile.reactive.messaging.Channel;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.Record;

@ApplicationScoped
public class ConsumerService {

    private final static Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    @Inject
    @Channel("outgoing-testevent")
    MutinyEmitter<Record<String, String>> emitterTestEvt;

    @Inject
    @Channel("outgoing-app_accepted_decoded")
    MutinyEmitter<Record<String, String>> emitterAppAccptdEvent;

    @Inject
    @Channel("outgoing-disclosure_sent_decoded")
    MutinyEmitter<Record<String, String>> emitterDisclosureSentEvent;

    @Inject
    @Channel("outgoing-evidence_submitted_decoded")
    MutinyEmitter<Record<String, String>> emitterEvidenceSubmitEvent;

    @Inject
    @Channel("outgoing-payment_received_decoded")
    MutinyEmitter<Record<String, String>> emitterPayRecvdEvent;

    @Inject
    @Channel("outgoing-review_scheduled_decoded")
    MutinyEmitter<Record<String, String>> emitterRevSchedEvent;

    @Inject
    @Channel("outgoing-vievent-decoded")
    MutinyEmitter<Record<String, String>> emitterViEvent;

    @Inject
    @Channel("outgoing-twelvehrevent-decoded")
    MutinyEmitter<Record<String, String>> emitterTwelveHourEvent;

    @Inject
    @Channel("outgoing-twentyfourhrevent-decoded")
    MutinyEmitter<Record<String, String>> emitterTwentyFourHourEvent;

    @Inject
    @Channel("outgoing-gis-geolocation-decoded")
    MutinyEmitter<Record<String, String>> emitterGisGeolocationEvent;

    private final HashMap<String, MutinyEmitter<Record<String, String>>> emitterMap = new HashMap<>();

    private void initializeMap() {
        emitterMap.put("sample", emitterTestEvt);
        emitterMap.put("app_accepted", emitterAppAccptdEvent);
        emitterMap.put("disclosure_sent", emitterDisclosureSentEvent);
        emitterMap.put("evidence_submitted", emitterEvidenceSubmitEvent);
        emitterMap.put("payment_received", emitterPayRecvdEvent);
        emitterMap.put("review_scheduled", emitterRevSchedEvent);

        emitterMap.put("vi_submitted", emitterViEvent);
        emitterMap.put("12hr_submitted", emitterTwelveHourEvent);
        emitterMap.put("24hr_submitted", emitterTwentyFourHourEvent);
        emitterMap.put("gis_geolocation", emitterGisGeolocationEvent);
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