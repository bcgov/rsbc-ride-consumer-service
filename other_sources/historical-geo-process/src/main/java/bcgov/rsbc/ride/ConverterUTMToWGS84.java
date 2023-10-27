package bcgov.rsbc.ride;

//import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationAdapter;
//import bcgov.rsbc.ride.kafka.models.PreciseGeolocationRecord;
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
    public double[] convert(String x, String y) {
        log.info("Converting UTM coordinates to WGS84 coordinates");
        String easting = x;
        String northing = y;
        double[] respval=new double[2];

        // Create a Coordinate Reference and a ProjCoordinate object to represent this coordinates
        int zone = 10;
        CRSFactory crsFactory = new CRSFactory();
        ProjCoordinate latLongCoord = new ProjCoordinate();
        CoordinateReferenceSystem utmCrs = crsFactory.createFromName("EPSG:326" + zone);
        ProjCoordinate utmCoord = new ProjCoordinate(Double.parseDouble(easting), Double.parseDouble(northing));
        utmCrs.getProjection().inverseProject(utmCoord, latLongCoord);

        double latitude = latLongCoord.y;
        double longitude = latLongCoord.x;
        respval[0]=latitude;
        respval[1]=longitude;

        // Create a Google Maps URL with the converted latitude, longitude, and UTM zone
        log.info("Google Maps URL: http://maps.google.com/maps?f=q&hl=en&geocode=&q=" + latitude + "," + longitude +
        "&ie=UTF8&ll=" + latitude + "," + longitude + "&spn=0.027108,0.109177&z=" + zone);

        return respval;
    }
}