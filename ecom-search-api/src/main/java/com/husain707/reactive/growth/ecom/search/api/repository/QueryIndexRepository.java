package com.husain707.reactive.growth.ecom.search.api.repository;

import com.husain707.reactive.growth.ecom.search.common.domain.QueryIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueryIndexRepository extends ReactiveElasticsearchRepository<QueryIndex, String> {
}