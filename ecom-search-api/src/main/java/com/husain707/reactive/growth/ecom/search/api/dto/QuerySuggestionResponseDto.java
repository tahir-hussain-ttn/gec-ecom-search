package com.husain707.reactive.growth.ecom.search.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class QuerySuggestionResponseDto implements Serializable {

    private String id;
    @JsonProperty("query_text")
    private String queryText;

    public QuerySuggestionResponseDto(String id, String queryText) {
        this.id = id;
        this.queryText = queryText;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
}