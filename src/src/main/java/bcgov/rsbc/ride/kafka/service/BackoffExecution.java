package bcgov.rsbc.ride.kafka.service;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Service to retry API calls with exponential backoff
 */
@Slf4j
@ApplicationScoped
public class BackoffExecution {

    private static final Logger logger = Logger.getLogger(RideAdapterService.class);
    public CompletableFuture<HttpResponse<Buffer>> executionWithRetry(
            String eventName, Supplier<CompletableFuture<HttpResponse<Buffer>>> operation, BackoffConfig backoffConfig) {
        return CompletableFuture.supplyAsync(() -> {
            int retryCount = 0;
            long delay = backoffConfig.retryIntervalMilliseconds;
            while (true) {
                try {
                    return operation.get().get(backoffConfig.timeoutSeconds, TimeUnit.SECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    retryCount++;
                    logger.warn("Retry count: " + retryCount + " for event: " + eventName, e);
                    if (retryCount >= backoffConfig.maxRetries) {
                        logger.error("Max retries reached. Aborting, for event: " + eventName);
                        throw new RuntimeException(e);
                    }
                    logger.error("Error during API call. Retrying in " + delay + " seconds. Retry count: " + retryCount);
                    try {
                        logger.warn("Sleep for" + delay + "seconds");
                        Thread.sleep(delay);
                        delay = Math.min(delay * 2, backoffConfig.maxDelayMilliseconds); // exponential backoff
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    public static class BackoffConfig {
        private final int maxRetries;
        private final int timeoutSeconds;
        private final long retryIntervalMilliseconds;
        private final int maxDelayMilliseconds;
    }
}
