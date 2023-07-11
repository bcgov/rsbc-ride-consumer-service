package bcgov.rsbc.ride.kafka.service;

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

@ApplicationScoped
public class etkConsumerService {
    private final static Logger logger = LoggerFactory.getLogger(etkConsumerService.class);
}
