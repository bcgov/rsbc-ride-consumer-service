package bcgov.rsbc.ride.kafka.converter;

import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationAdapter;
import bcgov.rsbc.ride.kafka.models.PreciseGeolocationRecord;
import lombok.extern.slf4j.Slf4j;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class converts UTM coordinates to latitude and longitude coordinates.
 * It converts LMD Servers' coordinates to Google Maps coordinates (WGS84).
 */
@Slf4j
@ApplicationScoped
public class ConverterUTMToWGS84 {
    public ApproximateGeolocationAdapter convert(PreciseGeolocationRecord event) {
        log.info("Converting UTM coordinates to WGS84 coordinates");
        String easting = event.getXValue();
        String northing = event.getYValue();

        if (easting.equals("0") || northing.equals("0"))
        {
           return ApproximateGeolocationAdapter.builder()
                .business_program("ETK")
                .business_type("violation")
                .business_id(event.getTicketNumber())
                .long$(0)
                .lat(0)
                .requested_address("")
                .submitted_address("")
                .full_address("")
                .databc_long("")
                .databc_lat("")
                .databc_score("")
                .build(); 
        }else{
            // Create a Coordinate Reference and a ProjCoordinate object to represent this coordinates
        int zone = 10;
        CRSFactory crsFactory = new CRSFactory();
        ProjCoordinate latLongCoord = new ProjCoordinate();
        CoordinateReferenceSystem utmCrs = crsFactory.createFromName("EPSG:326" + zone);
        ProjCoordinate utmCoord = new ProjCoordinate(Double.parseDouble(easting), Double.parseDouble(northing));
        utmCrs.getProjection().inverseProject(utmCoord, latLongCoord);

        String originalLatitude = String.valueOf(latLongCoord.y);
        String originalLongitude = String.valueOf(latLongCoord.x);

        String latitude = originalLatitude.substring(0, Math.min(originalLatitude.length(), 15));
        String longitude = originalLongitude.substring(0, Math.min(originalLongitude.length(), 15));

        log.debug("Original latitude: {}; trimmed latitude up to 15 characters: {}", originalLatitude, latitude);
        log.debug("Original longitude: {}; trimmed longitude up to 15 characters: {}", originalLongitude, longitude);

        // Create a Google Maps URL with the converted latitude, longitude, and UTM zone
        log.info("Google Maps URL: http://maps.google.com/maps?f=q&hl=en&geocode=&q=" + latitude + "," + longitude +
        "&ie=UTF8&ll=" + latitude + "," + longitude + "&spn=0.027108,0.109177&z=" + zone);

        return ApproximateGeolocationAdapter.builder()
                .business_program("ETK")
                .business_type("violation")
                .business_id(event.getTicketNumber())
                .long$(longitude)
                .lat(latitude)
                .requested_address("")
                .submitted_address("")
                .full_address("")
                .databc_long("")
                .databc_lat("")
                .databc_score("")
                .build();
        }

        
    }
}
