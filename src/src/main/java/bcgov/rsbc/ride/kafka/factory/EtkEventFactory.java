package bcgov.rsbc.ride.kafka.factory;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EtkEventFactory {


    Map<Class<Object>, EtkEventHandler> handlerMap = new HashMap<>();

    EtkEventFactory(List<EtkEventHandler> eventTypeHandlers) {
        for (EtkEventHandler event : eventTypeHandlers) {
            List<Class<Object>> eventTypeList = event.getEventTypeToHandler();
            for (Class<Object> eventType: eventTypeList) {
                handlerMap.put(eventType, event);
            }
        }
    }

    public <S, T> EtkEventHandler<S, T> getHadlerByEventType(Class<T> eventType) {
        return handlerMap.get(eventType);
    }

}




