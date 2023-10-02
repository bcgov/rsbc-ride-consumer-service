package bcgov.rsbc.ride.kafka.events;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import bcgov.rsbc.ride.kafka.core.PreciseGeolocationAvroMixin;
import bcgov.rsbc.ride.kafka.factory.EtkEventHandler;
import bcgov.rsbc.ride.kafka.models.*;
import bcgov.rsbc.ride.kafka.service.RideAdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import bcgov.rsbc.ride.kafka.service.ReconService;
import bcgov.rsbc.ride.kafka.converter.ConverterBCAlbersToWGS84;
import bcgov.rsbc.ride.kafka.converter.ConverterUTMToWGS84;

@Slf4j
@ApplicationScoped
public class GeolocationEvent extends EtkEventHandler<String, PreciseGeolocationRecord>{

    private static final Logger logger = Logger.getLogger(GeolocationEvent.class);

    @Inject
    RideAdapterService rideAdapterService;

    @Inject
    ReconService reconService;

    @Inject
    ConverterUTMToWGS84 converterUTMToWGS84;

    @Inject
    ConverterBCAlbersToWGS84 converterBCAlbersToWGS84;

    @Inject
    CustomObjectMapper customObjectMapper;

    @ConfigProperty(name = "ride.adapter.primarykey.geolocation")
    Optional<List<String>> primaryKey;

    @Override
    protected PreciseGeolocationRecord mapEvent(String preciseGeolocation) throws JsonProcessingException {
        return customObjectMapper.getObjectMapper()
                .addMixIn(PreciseGeolocationRecord.class, PreciseGeolocationAvroMixin.class)
                .readValue(preciseGeolocation, PreciseGeolocationRecord.class);
    }

    @Override
    public void execute(PreciseGeolocationRecord event, String eventId) {
        logger.info("GeolocationRequest Event received: " + event);

        ApproximateGeolocationAdapter geolocation = event.getServerCode().equalsIgnoreCase("LMD") ?
                converterUTMToWGS84.convert(event) : converterBCAlbersToWGS84.convert(event);

        reconService.updateMainStagingStatus(eventId,"consumer_geolocation_process");
        rideAdapterService.sendData(List.of(geolocation.toJson()), eventId,
                "gis","geolocations", primaryKey.orElse(null), 5000);

    }
}
