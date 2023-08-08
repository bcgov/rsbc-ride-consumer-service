package bcgov.rsbc.ride.kafka.factory;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface EtkEventHandler<S, T> {
    List<Class<T>> getEventTypeToHandler();
    T mapperEvent(S input) throws JsonProcessingException;
    void execute(T event);

}
