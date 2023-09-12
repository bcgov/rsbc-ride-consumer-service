package bcgov.rsbc.ride.kafka.service;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import bcgov.rsbc.ride.kafka.models.ReconStatusPayload;
import bcgov.rsbc.ride.kafka.models.ReconStatusPayloadData;

import java.util.List;
import java.util.concurrent.CompletionStage;


@Slf4j
@ApplicationScoped
public class ReconService {

    private static final Logger logger = Logger.getLogger(ReconService.class);

    @ConfigProperty(name = "ride.reconsvc.api.port")
    private Integer PORT;

    @ConfigProperty(name = "ride.reconsvc.api.host")
    private String HOST;

    @Inject
    Vertx vertx;


    public CompletionStage<HttpResponse<Buffer>> updateMainStagingStatus(String eventId,String statusVal) {

        WebClient webClient = WebClient.create(vertx);
        JSONObject payload = getStatusUpdPayload(eventId,statusVal);
        logger.info("Calling Recon service status update with payload: " + payload);
//        concatenate eventid to string
        String apiPath="/updateevent/"+eventId;

        return webClient.patch(PORT, HOST, apiPath)
                .putHeader("Content-Type", "application/json")
                .sendJson(payload).toCompletionStage().thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                        logger.error("Error calling Recon Svc API: " + resp.statusCode() + " " + resp.statusMessage());
                        return null;
                    }
                    logger.info("Updating Recon staging finished.");
                    return resp;
                });
    }

    private JSONObject getStatusUpdPayload(String eventId,String statusVal) {
        JSONObject payloadobj=new JSONObject();
        payloadobj.put("messageStatus",statusVal);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionName", "mainstaging");
        jsonObject.put("payloaddata", payloadobj);
        return jsonObject;
    }
}


