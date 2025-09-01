package com.husain707.reactive.growth.ecom.search.indexer.consumer;

import com.husain707.reactive.growth.ecom.search.indexer.event.CategoryEventPayload;
import com.husain707.reactive.growth.ecom.search.indexer.service.QueryGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CategoryUpdateConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryUpdateConsumer.class);

    private final QueryGenerationService queryGenerationService;

    public CategoryUpdateConsumer(QueryGenerationService queryGenerationService) {
        this.queryGenerationService = queryGenerationService;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.category-update}",
            groupId = "gec_consumer_category_update",
            containerFactory = "categoryUpdateKafkaListenerContainerFactory"
    )
    public void consume(CategoryEventPayload event) {
        LOGGER.info("Processing category update event with ID: {}", event.getEventId());

        // Generate and save search queries for the new/updated category name
        if (event.getData().getName() != null) {
            queryGenerationService.generateAndSaveForCategory(event.getData().getName());
        }
    }
}