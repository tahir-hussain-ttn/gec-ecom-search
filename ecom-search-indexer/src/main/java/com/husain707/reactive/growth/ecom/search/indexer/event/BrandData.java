package com.husain707.reactive.growth.ecom.search.indexer.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class BrandData {

    @JsonProperty("brand_id")
    private long brandId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("old_name")
    private String oldName;

    @JsonProperty("categories")
    private Set<String> categories;

    // Getters and Setters
    public long getBrandId() { return brandId; }
    public void setBrandId(long brandId) { this.brandId = brandId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOldName() { return oldName; }
    public void setOldName(String oldName) { this.oldName = oldName; }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }
}