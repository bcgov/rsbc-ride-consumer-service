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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import bcgov.rsbc.ride.kafka.service.ReconService;

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

//    @WithSpan
    public CompletionStage<HttpResponse<Buffer>> sendData(List<Object> persistenceList, String schema, String tableName, List<String> primaryKey,String key) {

        WebClient webClient = WebClient.create(vertx);
        String payload = getPayload(persistenceList, schema, tableName, primaryKey);
        logger.info("Calling Ride DB Adapter API with payload: " + payload);

         return webClient.post(PORT, HOST, "/upsertdata")
                .putHeader("Content-Type", "application/json")
                .sendJson(payload).toCompletionStage().thenApply(resp -> {
                    if (resp.statusCode() != 200) {
                        logger.error("Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage());
                        reconService.sendErrorRecords(key,"Error calling Ride DB Adapter API: " + resp.statusCode() + " " + resp.statusMessage());
                        reconService.updateMainStagingStatus(key,"consumer_error");
                        return null;
                    }
                    logger.info("Persistence finished.");
                    reconService.updateMainStagingStatus(key,"consumer_bi_sent");
                    return resp;
                });
    }

    private String getPayload(List<Object> persistenceList, String schema, String tableName, List<String> primaryKey) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tablename", tableName);
        jsonObject.put("schema", schema);
        jsonObject.put("destination", "bi");
        jsonObject.put("data", persistenceList);
        jsonObject.put("source", "etk_consumer");
        if (primaryKey != null) {
            jsonObject.put("primarykeys", primaryKey);
        }
        return jsonObject.toJSONString();
    }
}
