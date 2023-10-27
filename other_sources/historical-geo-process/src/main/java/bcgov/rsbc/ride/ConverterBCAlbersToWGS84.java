package bcgov.rsbc.ride;

//import bcgov.rsbc.ride.kafka.models.ApproximateGeolocationAdapter;
//import bcgov.rsbc.ride.kafka.models.PreciseGeolocationRecord;
import lombok.extern.slf4j.Slf4j;
import org.osgeo.proj4j.*;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class converts BC Albers coordinates to latitude and longitude coordinates.
 * It converts VIR and NSE Servers' coordinates to Google Maps coordinates (WGS84).
 */
@Slf4j
@ApplicationScoped
public class ConverterBCAlbersToWGS84{
    public double[] convert(String x, String y) {
        log.info("Converting BC Albers coordinates to WGS84 coordinates");
        String easting = x;
        String northing = y;
        double[] respval=new double[2];

        // Create BC Albers and WGS84 coordinate systems
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem albersBC = crsFactory.createFromName("EPSG:3005");
        CoordinateReferenceSystem wgs84 = crsFactory.createFromParameters("WGS84", "+proj=latlong +datum=WGS84 +no_defs");
        CoordinateTransformFactory transformFactory = new CoordinateTransformFactory();
        CoordinateTransform transform = transformFactory.createTransform(albersBC, wgs84);

        // Perform the conversion
        ProjCoordinate albersCoord = new ProjCoordinate(Double.parseDouble(easting), Double.parseDouble(northing));
        ProjCoordinate wgs84Coord = new ProjCoordinate();
        transform.transform(albersCoord, wgs84Coord);

        double latitude = wgs84Coord.y;
        double longitude = wgs84Coord.x;
        log.info("Latitude: " + latitude + ", Longitude: " + longitude);
        respval[0]=latitude;
        respval[1]=longitude;

        // Create Google Maps URL
        log.info("Google Maps URL: http://maps.google.com/maps?f=q&hl=en&geocode=&q=" + latitude + "," + longitude +
        "&ie=UTF8&ll=" + latitude + "," + longitude + "&spn=0.027108,0.109177");

        return respval;

    }
}