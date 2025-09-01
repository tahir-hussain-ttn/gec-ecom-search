package com.husain707.reactive.growth.ecom.search.indexer.service;

import com.husain707.reactive.growth.ecom.search.common.domain.QueryIndex;
import com.husain707.reactive.growth.ecom.search.indexer.repository.QueryIndexRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class QueryGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGenerationService.class);

    private final QueryIndexRepository queryIndexRepository;

    // Static lists for query generation
    private static final List<String> TEMPLATES_WITH_BRAND_AND_CATEGORIES = Arrays.asList(
            "%s %s", // e.g., Samsung Mobiles
            "latest %s %s" // e.g., latest Samsung Mobiles
    );
    private static final List<String> TEMPLATES_WITH_BRAND_CATEGORIES_AND_PRICE = Arrays.asList(
            "%s %s below %d", // e.g., Samsung Mobiles below 30000
            "%s %s under %d"  // e.g., Samsung Mobiles under 30000
    );
    private static final List<String> TEMPLATES_WITH_CATEGORY_AND_PRICE = Arrays.asList(
            "%s below %d",
            "%s under %d"
    );
    private static final List<String> TEMPLATES_WITH_CATEGORY = Arrays.asList(
            "%s", // e.g., Samsung Mobiles
            "latest %s" // e.g., latest Samsung Mobiles
    );
    private static final List<Integer> PRICE_POINTS = Arrays.asList(10000, 20000, 30000, 50000, 100000);
    private static final List<String> GENERIC_TERMS = Arrays.asList("electronics", "mobiles", "phones", "products");

    public QueryGenerationService(QueryIndexRepository queryIndexRepository) {
        this.queryIndexRepository = queryIndexRepository;
    }

    public void generateAndSaveForBrand(String brandName, Set<String> categories) {
        if (brandName == null || brandName.trim().isEmpty()) {
            return;
        }
        LOGGER.info("Generating search queries for brand: {}", brandName);

        List<QueryIndex> queriesToSave = new ArrayList<>();

        if (!CollectionUtils.isEmpty(categories)) {
            List<Set<String>> categorySubsets = getSubsets(new ArrayList<>(categories));

            for (Set<String> subset : categorySubsets) {
                List<String> subsetAsList = new ArrayList<>(subset);
                String categoryQueryPart = String.join(" ", subsetAsList);

                TEMPLATES_WITH_BRAND_AND_CATEGORIES.forEach(template ->
                        queriesToSave.add(buildQuery(String.format(template, brandName, categoryQueryPart), brandName, subsetAsList, null))
                );

                for (Integer price : PRICE_POINTS) {
                    TEMPLATES_WITH_BRAND_CATEGORIES_AND_PRICE.forEach(template ->
                            queriesToSave.add(buildQuery(String.format(template, brandName, categoryQueryPart, price), brandName, subsetAsList, price.doubleValue()))
                    );
                }
            }
        }

        // 2. Generate queries with generic terms
        GENERIC_TERMS.forEach(term -> queriesToSave.add(buildQuery(String.format("%s %s", brandName, term), brandName, Collections.singletonList(term), null)));

        // 3. Generate brand-only query
        queriesToSave.add(buildQuery(brandName, brandName, null, null));

        queryIndexRepository.saveAll(queriesToSave)
                .collectList()
                .doOnSuccess(savedItems -> LOGGER.info("Saved {} auto-generated queries for brand: {}", savedItems.size(), brandName))
                .doOnError(error -> LOGGER.error("Error saving auto-generated queries for brand: {}", brandName, error))
                .subscribe();
    }

    public void generateAndSaveForCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return;
        }
        LOGGER.info("Generating search queries for category: {}", categoryName);

        List<QueryIndex> queriesToSave = new ArrayList<>();

        TEMPLATES_WITH_CATEGORY.forEach(template ->
                queriesToSave.add(buildQuery(String.format(template, categoryName), null, Collections.singletonList(categoryName), null))
        );

        // 2. Generate queries for the category with price points (no brand)
        for (Integer price : PRICE_POINTS) {
            TEMPLATES_WITH_CATEGORY_AND_PRICE.forEach(template ->
                    queriesToSave.add(buildQuery(String.format(template, categoryName, price), null, Collections.singletonList(categoryName), price.doubleValue()))
            );
        }

        // 3. Generate category-only query
        queriesToSave.add(buildQuery(categoryName, null, Collections.singletonList(categoryName), null));

        queryIndexRepository.saveAll(queriesToSave) // saveAll returns a Flux of saved entities
                .collectList() // Consume the flux and collect all saved items into a list. This ensures the save operation is fully executed.
                .doOnSuccess(savedItems -> LOGGER.info("Saved {} auto-generated queries for category: {}", savedItems.size(), categoryName))
                .doOnError(error -> LOGGER.error("Error saving auto-generated queries for category: {}", categoryName, error))
                .subscribe();
    }

    private QueryIndex buildQuery(String queryText, String brand, List<String> categories, Double maxPrice) {
        QueryIndex queryIndex = new QueryIndex();
        queryIndex.setQueryText(new Completion(new String[]{queryText}));
        queryIndex.setNormalizedQuery(queryText.toLowerCase());

        QueryIndex.QueryFilters filters = new QueryIndex.QueryFilters();
        filters.setBrand(brand);
        if (!CollectionUtils.isEmpty(categories)) {
            filters.setCategories(categories);
        }
        if (maxPrice != null) {
            QueryIndex.PriceFilter priceFilter = new QueryIndex.PriceFilter();
            priceFilter.setMax(maxPrice);
            filters.setPrice(priceFilter);
        }
        queryIndex.setFilters(filters);

        queryIndex.setSearchCount(0L);
        queryIndex.setLastSearchedAt(Instant.now());
        queryIndex.setAutoGenerated(true);
        queryIndex.setSuggestWeight(100 - (!CollectionUtils.isEmpty(categories) ? 20 : 0) - (maxPrice != null ? 30 : 0));

        return queryIndex;
    }

    private <T> List<Set<T>> getSubsets(List<T> set) {
        List<Set<T>> subsets = new ArrayList<>();
        int n = set.size();
        // Run a loop for printing all 2^n subsets one by one
        for (int i = 0; i < (1 << n); i++) {
            Set<T> subset = new java.util.HashSet<>();
            // Print current subset
            for (int j = 0; j < n; j++)
                if ((i & (1 << j)) > 0)
                    subset.add(set.get(j));
            if (!subset.isEmpty()) {
                subsets.add(subset);
            }
        }
        return subsets;
    }
}