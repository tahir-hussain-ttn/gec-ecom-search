package com.husain707.reactive.growth.ecom.search.indexer.consumer;

import com.husain707.reactive.growth.ecom.search.indexer.event.BrandUpdateEvent;
import com.husain707.reactive.growth.ecom.search.indexer.service.QueryGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BrandUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandUpdateConsumer.class);

    private final QueryGenerationService queryGenerationService;

    public BrandUpdateConsumer(QueryGenerationService queryGenerationService) {
        this.queryGenerationService = queryGenerationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.brand-update}",
            groupId = "gec_consumer_brand_update",
            containerFactory = "brandUpdateKafkaListenerContainerFactory"
    )
    public void consume(BrandUpdateEvent event) {
        LOGGER.info("Processing brand update event with ID: {}", event.getEventId());

        // Generate and save search queries for the new/updated brand name
        if (event.getData().getName() != null) {
            queryGenerationService.generateAndSaveForBrand(event.getData().getName(), event.getData().getCategories());
        }
    }
}