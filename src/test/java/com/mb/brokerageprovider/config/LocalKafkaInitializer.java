package com.mb.brokerageprovider.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

public class LocalKafkaInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.3"));

    static {
        Startables.deepStart(kafka).join();
    }

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext context) {
        TestPropertyValues.of("KAFKA_CONSUMER_BOOTSTRAP_SERVERS=%s".formatted(kafka.getBootstrapServers()),
                        "KAFKA_PRODUCER_BOOTSTRAP_SERVERS=%s".formatted(kafka.getBootstrapServers()))
                .applyTo(context.getEnvironment());
    }
}