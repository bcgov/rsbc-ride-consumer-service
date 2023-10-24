package bcgov.rsbc.ride.kafka.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class RideAdapterService {

    private static final Logger logger = Logger.getLogger(RideAdapterService.class);

    @ConfigProperty(name = "ride.adapter.api.port")
    private Integer PORT;
    @ConfigProperty(name = "ride.adapter.api.host")
    private String HOST;

    @Inject
    ReconService reconService;

    @Inject
    Vertx vertx;


    @WithSpan
    public CompletableFuture<HttpResponse<Buffer>> sendData(List<?> persistenceList, String eventId, String schema, String tableName,
                                                            List<String> primaryKey, int timeoutMilliseconds) {

        WebClient webClient = WebClient.create(vertx);
        JSONObject payload = getPayload(persistenceList, eventId, schema, tableName, primaryKey);
        try {
            String payloadString = payload.toString();
            if (payloadString.contains("violationformnumber")) {
                payloadString = payloadString.replace("violationformnumber", "e_violation_form_number");
                JSONParser parser = new JSONParser();
//                JSONObject json1 = (JSONObject) parser.parse(payloadString);
//                payload = json1;
                payload = (JSONObject) parser.parse(payloadString);
            }
        } catch (Exception e) {
            logger.error("Error transforming violation form number: " + e.getMessage() + ", eventId:" + eventId);
        }

        logger.info("Calling Ride DB Adapter API with payload: " + payload + ", eventId: " + eventId);

        return webClient
                .post(PORT, HOST,"/upsertdata")
                .timeout(timeoutMilliseconds)
                .putHeader("Content-Type", "application/json")
                .sendJson(payload)
                .toCompletionStage()
                .exceptionally(e -> {
                    logger.error("Error calling Ride DB Adapter API: " + e.getMessage() + ", eventId:" + eventId);
                    reconService.updateMainStagingStatus(eventId,"consumer_error");
                    reconService.sendErrorRecords(eventId,"Error calling Ride DB Adapter API: " + e.getMessage(),"others");
                    return null;
                })
                .toCompletableFuture()
                .thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                       logger.error("Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage() + ", eventId:" + eventId);
                        reconService.updateMainStagingStatus(eventId,"consumer_error");
                        reconService.sendErrorRecords(eventId,"Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage(),"others");
                        return null;
                    }
                    logger.info("Persistence finished" + ", eventId:" + eventId);
                    reconService.updateMainStagingStatus(eventId,"consumer_bi_sent");
                    return resp;
                });
    }


    private JSONObject getPayload(List<?> persistenceList, String eventId, String schema, String tableName, List<String> primaryKey) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tablename", tableName);
        jsonObject.put("schema", schema);
        jsonObject.put("destination", "bi");
        jsonObject.put("data", persistenceList);
        jsonObject.put("source", "etk_consumer");
        jsonObject.put("eventid", eventId);
        if (primaryKey != null) {
            jsonObject.put("primarykeys", primaryKey);
        }
        return jsonObject;
    }
}
