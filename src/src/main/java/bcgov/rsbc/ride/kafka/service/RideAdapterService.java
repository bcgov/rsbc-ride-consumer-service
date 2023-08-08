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
import java.util.concurrent.CompletionStage;

@Slf4j
@ApplicationScoped
public class RideAdapterService {

    private static final Logger logger = Logger.getLogger(RideAdapterService.class);

    @ConfigProperty(name = "ride.adapter.api.port")
    private Integer PORT;
    @ConfigProperty(name = "ride.adapter.api.host")
    private String HOST;

    @WithSpan
    public CompletionStage<HttpResponse<Buffer>> sendData(Object persistenceObject, String tableName) {

        Vertx vertx = Vertx.vertx();
        WebClient webClient = WebClient.create(vertx);
        String payload = getPayload(persistenceObject, tableName);
        logger.info("Calling Ride DB Adapter API with payload: " + payload);

        return webClient.post(PORT, HOST, "/upsertdata")
                .putHeader("Content-Type", "application/json")
                .sendJson(payload).toCompletionStage().toCompletableFuture();
    }

    private String getPayload(Object persistenceObject, String tableName) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tablename", tableName);
        jsonObject.put("schema", "schema_val");
        jsonObject.put("destination", "bi");
        jsonObject.put("data", persistenceObject);
        jsonObject.put("source", "etk_consumer");
        return jsonObject.toJSONString();
    }
}
