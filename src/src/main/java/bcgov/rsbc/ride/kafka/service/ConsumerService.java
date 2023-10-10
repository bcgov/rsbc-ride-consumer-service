package bcgov.rsbc.ride.kafka.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
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

    public boolean publishEventtoDecodedTopic(String eventPayload,String eventType,Long uid) {

        switch(eventType) {
            case "sample":
                logger.info(eventPayload);
                emitterTestEvt.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                break;
            case "app_accepted":
                try {
                    logger.info(eventPayload);
                    emitterAppAccptdEvent.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                } catch (Exception e) {
                    logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
                }
                break;
            case "disclosure_sent":
                try {
                    logger.info(eventPayload);
                    emitterDisclosureSentEvent.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                } catch (Exception e) {
                    logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
                }
                break;
            case "evidence_submitted":
                try {
                    logger.info(eventPayload);
                    emitterEvidenceSubmitEvent.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                } catch (Exception e) {
                    logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
                }
                break;
            case "payment_received":
                try {
                    logger.info(eventPayload);
                    emitterPayRecvdEvent.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                } catch (Exception e) {
                    logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
                }
                break;
            case "review_scheduled":
                try {
                    logger.info(eventPayload);
                    emitterRevSchedEvent.send(Record.of(uid.toString(), eventPayload)).await().atMost(Duration.ofSeconds(5));
                } catch (Exception e) {
                    logger.error("Exception occurred while sending decoded event, exception details: {}", e.toString() + "; " + e.getMessage());
                }
                break;
            default:
                logger.info(eventPayload);
                logger.info("no matches for the event type");
        }
        return false;
    }
}