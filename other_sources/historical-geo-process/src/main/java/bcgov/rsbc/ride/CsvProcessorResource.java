package bcgov.rsbc.ride;

//import javax.inject.Inject;
import jakarta.inject.Inject;
//import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
//import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/process-csv")
public class CsvProcessorResource {

    @Inject
    CsvProcessorService csvProcessorService;

    @ConfigProperty(name = "ride.geolocation.file")
    private String filepath;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String processCsv() {
        try {
            csvProcessorService.processCsv(filepath);
            return "Processing successful!";
        } catch (Exception e) {
            return "Error processing CSV: " + e.getMessage();
        }
    }
}
