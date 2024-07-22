package bcgov.rsbc.ride.kafka.converter;

import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationAdapter;
import bcgov.rsbc.ride.kafka.models.PreciseGeolocationRecord;
import lombok.extern.slf4j.Slf4j;
import org.osgeo.proj4j.*;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * This class converts BC Albers coordinates to latitude and longitude coordinates.
 * It converts VIR and NSE Servers' coordinates to Google Maps coordinates (WGS84).
 */
@Slf4j
@ApplicationScoped
public class ConverterBCAlbersToWGS84{
    public ApproximateGeolocationAdapter convert(PreciseGeolocationRecord event) {
        log.info("Converting BC Albers coordinates to WGS84 coordinates");
        Double easting = Double.parseDouble(event.getXValue().trim());
        Double northing = Double.parseDouble(event.getYValue().trim());

        if (easting.equals(0d) || northing.equals(0d))

        {
            return ApproximateGeolocationAdapter.builder()
                    .business_program("ETK")
                    .business_type("violation")
                    .business_id(event.getTicketNumber())
                    // .long$(String.valueOf(longitude))
                    // .lat(String.valueOf(latitude))
                    .long$("")
                    .lat("")
                    .requested_address("")
                    .submitted_address("")
                    .full_address("")
                    .databc_long("")
                    .databc_lat("")
                    .databc_score("")
                    .build();

        }else{
            // Create BC Albers and WGS84 coordinate systems
            CRSFactory crsFactory = new CRSFactory();
            CoordinateReferenceSystem albersBC = crsFactory.createFromName("EPSG:3005");
            CoordinateReferenceSystem wgs84 = crsFactory.createFromParameters("WGS84", "+proj=latlong +datum=WGS84 +no_defs");
            CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
            CoordinateTransform transform = transformFactory.createTransform(albersBC, wgs84);

            // Perform the conversion
            ProjCoordinate albersCoord = new ProjCoordinate(easting, northing);
            ProjCoordinate wgs84Coord = new ProjCoordinate();
            transform.transform(albersCoord, wgs84Coord);

            // double latitude = wgs84Coord.y;
            // double longitude = wgs84Coord.x;

            String originalLatitude = String.valueOf(wgs84Coord.y);
            String originalLongitude = String.valueOf(wgs84Coord.x);

            String latitude = originalLatitude.substring(0, Math.min(originalLatitude.length(), 15));
            String longitude = originalLongitude.substring(0, Math.min(originalLongitude.length(), 15));

            log.debug("Original latitude: {}; trimmed latitude up to 15 characters: {}", originalLatitude, latitude);
            log.debug("Original longitude: {}; trimmed longitude up to 15 characters: {}", originalLongitude, longitude);


            // Create Google Maps URL
            log.info("Google Maps URL: http://maps.google.com/maps?f=q&hl=en&geocode=&q=" + latitude + "," + longitude +
                    "&ie=UTF8&ll=" + latitude + "," + longitude + "&spn=0.027108,0.109177");

            return ApproximateGeolocationAdapter.builder()
                    .business_program("ETK")
                    .business_type("violation")
                    .business_id(event.getTicketNumber())
                    // .long$(String.valueOf(longitude))
                    // .lat(String.valueOf(latitude))
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
