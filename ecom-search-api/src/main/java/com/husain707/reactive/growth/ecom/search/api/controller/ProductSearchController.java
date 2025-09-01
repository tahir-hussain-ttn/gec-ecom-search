package com.husain707.reactive.growth.ecom.search.api.controller;

import com.husain707.reactive.growth.ecom.search.api.dto.QuerySuggestionResponseDto;
import com.husain707.reactive.growth.ecom.search.api.service.ProductSearchService;
import com.husain707.reactive.growth.ecom.search.common.domain.ProductIndex;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/products")
public class ProductSearchController {

    private final ProductSearchService searchService;

    public ProductSearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public Flux<ProductIndex> searchProducts(@RequestParam("q") String query) {
        return searchService.searchProducts(query);
    }

    @GetMapping("/suggest")
    public Flux<QuerySuggestionResponseDto> suggestQueries(@RequestParam("prefix") String prefix){
        return searchService.suggestQueries(prefix);
    }

    @GetMapping("/search/by-id/{queryId}")
    public Flux<ProductIndex> searchProductsByQueryId(
            @PathVariable(name = "queryId") String queryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return searchService.searchProductsByQueryId(queryId, page, size);
    }
}