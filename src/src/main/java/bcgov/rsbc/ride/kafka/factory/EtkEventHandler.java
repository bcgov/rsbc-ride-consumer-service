package bcgov.rsbc.ride.kafka.factory;

import bcgov.rsbc.ride.kafka.core.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.*;

@ApplicationScoped
public abstract class EtkEventHandler<S, T> {

    @Inject
    CustomObjectMapper objectMapper;

    public Class<T> getEventsSubscribed() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                Type classType = actualTypeArguments[1];
                if (classType instanceof Class<?>) {
                    @SuppressWarnings("unchecked")
                    Class<T> rawType = (Class<T>) classType;
                    return rawType;
                }
            }
        }

        throw new IllegalArgumentException("Unable to determine the type parameter.");
    }
    public T map(S input) {
        try {
            return mapEvent(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    protected T mapEvent(S input) throws JsonProcessingException{
        Type type = getClass().getGenericSuperclass();
        Type classType = ((ParameterizedType) type).getActualTypeArguments()[1];
        Class<T> rawType = classType instanceof Class<?>
                ? (Class<T>) classType
                : (Class<T>) ((ParameterizedType) classType).getRawType();
        return objectMapper.getObjectMapper().readValue((String) input, rawType);
    }
    public abstract void execute(T event, String eventId);

    public void setEventId(T event, String eventId) {
        try {
            event.getClass().getDeclaredField("event_id").setAccessible(true);
            event.getClass().getMethod("setEventId", String.class).invoke(event, eventId);
        } catch (NoSuchFieldException| NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
    }
}
