package bcgov.rsbc.ride.kafka;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Singleton
public class CustomConfiguration {
    private final static Logger logger = LoggerFactory.getLogger(CustomConfiguration.class);
    @Produces
    @Singleton
    public MeterFilter configureAllRegistries() {
        return new MeterFilter() {
            @Override
            public MeterFilterReply accept(Meter.Id id) {
                if (id.getName().startsWith("kafka") &&
                    !id.getName().contains("records.consumed")) {
                    logger.trace("Denying meter with name: {}", id.getName());
                    return MeterFilterReply.DENY;
                }
                return MeterFilterReply.NEUTRAL;
            }
        };
    }
}
