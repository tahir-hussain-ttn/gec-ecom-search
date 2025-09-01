package com.husain707.reactive.growth.ecom.search.indexer.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoryEventData {

    @JsonProperty("category_id")
    private long categoryId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("old_name")
    private String oldName;

    // Getters and Setters
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOldName() { return oldName; }
    public void setOldName(String oldName) { this.oldName = oldName; }
}