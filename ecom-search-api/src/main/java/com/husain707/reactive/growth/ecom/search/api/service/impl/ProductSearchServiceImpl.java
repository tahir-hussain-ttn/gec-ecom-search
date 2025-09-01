package com.husain707.reactive.growth.ecom.search.api.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.husain707.reactive.growth.ecom.search.api.dto.QuerySuggestionResponseDto;
import com.husain707.reactive.growth.ecom.search.api.repository.ProductSearchRepository;
import com.husain707.reactive.growth.ecom.search.api.repository.QueryIndexRepository;
import com.husain707.reactive.growth.ecom.search.api.service.ProductSearchService;
import com.husain707.reactive.growth.ecom.search.common.domain.ProductIndex;
import com.husain707.reactive.growth.ecom.search.common.domain.QueryIndex;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ReactiveSearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository repository;
    private final QueryIndexRepository queryIndexRepository;
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public ProductSearchServiceImpl(ProductSearchRepository repository,
                                    ReactiveElasticsearchOperations reactiveElasticsearchOperations,
                                    QueryIndexRepository queryIndexRepository) {
        this.repository = repository;
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
        this.queryIndexRepository = queryIndexRepository;
    }

    @Override
    public Flux<ProductIndex> searchProducts(String query) {
        return repository.search(query);
    }

    /**
     * Provides autocomplete suggestions for a given search text prefix.
     *
     * @param prefix The incomplete search text.
     * @return A Flux of suggested queries.
     */
    @Override
    public Flux<QuerySuggestionResponseDto> suggestQueries(String prefix) {
        String suggesterName = "query-text-suggester";

        Suggester suggester = Suggester.of(s -> s
                .suggesters(suggesterName, FieldSuggester.of(fs -> fs
                        .prefix(prefix)
                        .completion(cs -> cs
                                .field("query_text")
                                .size(5)
                                .skipDuplicates(true)
                        )
                ))
        );

        // We only need suggestions, so we create a query that returns no documents
        // but includes the suggester.
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withSuggester(suggester)
                .withMaxResults(0)
                .build();

        return reactiveElasticsearchOperations.searchForHits(query, QueryIndex.class)
                .map(ReactiveSearchHits::getSuggest)
                .filter(java.util.Objects::nonNull)
                // Use the Spring Data wrapper classes consistently
                .map(suggest -> suggest.getSuggestion(suggesterName))
                .filter(java.util.Objects::nonNull)
                .flatMapMany(suggestion -> Flux.fromIterable(suggestion.getEntries()))
                .flatMap(entry -> Flux.fromIterable(entry.getOptions()))
                .map(option -> new QuerySuggestionResponseDto(((CompletionSuggestion.Entry.Option<?>) option).getSearchHit().getId(), option.getText()));
    }

    /**
     * Searches for products using a previously saved query from the QueryIndex.
     *
     * @param queryId The ID of the query in the 'gec_query' index.
     * @param page    The page number for pagination.
     * @param size    The number of results per page.
     * @return A Flux of ProductIndex matching the search criteria.
     */
    @Override
    public Flux<ProductIndex> searchProductsByQueryId(String queryId, int page, int size) {
        return queryIndexRepository.findById(queryId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Query with ID " + queryId + " not found")))
                .flatMapMany(queryIndex -> {
                    final QueryIndex.QueryFilters filters = queryIndex.getFilters();

                    if (filters == null) {
                        return Flux.empty();
                    }

                    Query query = buildQueryFromFilters(filters);

                    NativeQuery searchQuery = NativeQuery.builder()
                            .withQuery(query)
                            .withPageable(PageRequest.of(page, size))
                            .build();
                    System.out.println("Query: " + searchQuery.getQuery());

                    return reactiveElasticsearchOperations.search(searchQuery, ProductIndex.class)
                            .map(SearchHit::getContent);
                });
    }

    /**
     * Constructs an Elasticsearch bool query from the provided filter object.
     *
     * @param filters The filters to apply.
     * @return A fully constructed Elasticsearch Query object.
     */
    private Query buildQueryFromFilters(QueryIndex.QueryFilters filters) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        boolean hasFilters = false;

        // Brand filter
        if (filters.getBrand() != null && !filters.getBrand().isBlank()) {
            boolQueryBuilder.filter(f -> f.term(t -> t.field("brand").value(filters.getBrand())));
            hasFilters = true;
        }

        // Categories filter
        if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
            boolQueryBuilder.filter(f -> f.terms(t -> t.field("categories")
                    .terms(ts -> ts.value(filters.getCategories().stream().map(FieldValue::of).collect(Collectors.toList())))));
            hasFilters = true;
        }

        // Price filter
        if (filters.getPrice() != null) {
            boolQueryBuilder.filter(f -> f.range(r -> {
                r.field("price");
                if (filters.getPrice().getMin() != null) {
                    r.gte(co.elastic.clients.json.JsonData.of(filters.getPrice().getMin()));
                }
                if (filters.getPrice().getMax() != null) {
                    r.lte(co.elastic.clients.json.JsonData.of(filters.getPrice().getMax()));
                }
                return r;
            }));
            hasFilters = true;
        }

        // Attributes filter (nested)
        if (filters.getAttributes() != null && !filters.getAttributes().isEmpty()) {
            for (final QueryIndex.AttributeFilter attribute : filters.getAttributes()) {
                boolQueryBuilder.filter(f -> f.nested(n -> n
                        .path("attributes")
                        .query(nq -> nq.bool(nb -> nb
                                .must(m -> m.term(t -> t.field("attributes.key").value(attribute.getKey())))
                                .must(m -> m.term(t -> t.field("attributes.value").value(attribute.getValue())))
                        ))
                ));
            }
            hasFilters = true;
        }

        // If no filter clauses were added, this bool query would match all.
        // We prevent this by adding a clause that matches nothing.
        if (!hasFilters) {
            boolQueryBuilder.mustNot(mn -> mn.matchAll(ma -> ma));
        }

        return boolQueryBuilder.build()._toQuery();
    }
}