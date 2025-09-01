package com.husain707.reactive.growth.ecom.search.api.service;

import com.husain707.reactive.growth.ecom.search.api.dto.QuerySuggestionResponseDto;
import com.husain707.reactive.growth.ecom.search.common.domain.ProductIndex;
import reactor.core.publisher.Flux;

public interface ProductSearchService {
    Flux<ProductIndex> searchProducts(String query);
    Flux<QuerySuggestionResponseDto> suggestQueries(String prefix);
    Flux<ProductIndex> searchProductsByQueryId(String queryId, int page, int size);
}