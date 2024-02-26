package com.mb.brokerageprovider.config.aspect;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(999)
public class LogObservationHandler implements ObservationHandler<Observation.Context> {

    private static final Logger log = LoggerFactory.getLogger(LogObservationHandler.class);

    @Override
    public void onStart(Observation.Context context) {
        log.info("LogObservationHandler::onStart - Execution started. contextName: {}", context.getName());
        context.put("time", System.currentTimeMillis());
    }

    @Override
    public void onStop(Observation.Context context) {
        log.info("LogObservationHandler::onStop - Execution stopped. Name: {} Duration: {} ms", context.getName(), (System.currentTimeMillis() - context.getOrDefault("time", 0L)));
    }

    @Override
    public boolean supportsContext(@NonNull Observation.Context context) {
        return true;
    }
}
