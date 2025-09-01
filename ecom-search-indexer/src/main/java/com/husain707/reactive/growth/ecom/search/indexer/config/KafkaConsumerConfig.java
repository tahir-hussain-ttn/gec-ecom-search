package com.husain707.reactive.growth.ecom.search.indexer.config;

import com.husain707.reactive.growth.ecom.search.indexer.event.BrandUpdateEvent;
import com.husain707.reactive.growth.ecom.search.indexer.event.CategoryEventPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, CategoryEventPayload> categoryUpdateConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonDeserializer<CategoryEventPayload> deserializer = new JsonDeserializer<>(CategoryEventPayload.class);
        // This is the key change: Do not use the type information from the message headers.
        // This forces the deserializer to use the target type of the listener method.
        deserializer.setUseTypeHeaders(false);
        // It's still good practice to trust the package of the target type.
        deserializer.addTrustedPackages("com.husain707.reactive.growth.ecom.search.indexer.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConsumerFactory<String, BrandUpdateEvent> brandUpdateConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonDeserializer<BrandUpdateEvent> deserializer = new JsonDeserializer<>(BrandUpdateEvent.class);
        // This is the key change: Do not use the type information from the message headers.
        // This forces the deserializer to use the target type of the listener method.
        deserializer.setUseTypeHeaders(false);
        // It's still good practice to trust the package of the target type.
        deserializer.addTrustedPackages("com.husain707.reactive.growth.ecom.search.indexer.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CategoryEventPayload> categoryUpdateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CategoryEventPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(categoryUpdateConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BrandUpdateEvent> brandUpdateKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BrandUpdateEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(brandUpdateConsumerFactory());
        return factory;
    }
}