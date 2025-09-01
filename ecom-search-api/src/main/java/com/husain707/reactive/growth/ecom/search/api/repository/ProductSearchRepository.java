package com.husain707.reactive.growth.ecom.search.api.repository;

import com.husain707.reactive.growth.ecom.search.common.domain.ProductIndex;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

public interface ProductSearchRepository extends ReactiveElasticsearchRepository<ProductIndex, String> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"description\", \"brand\", \"categories\"]}}")
    Flux<ProductIndex> search(String query);
}