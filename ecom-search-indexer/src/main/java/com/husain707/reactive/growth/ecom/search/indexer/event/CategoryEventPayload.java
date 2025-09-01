package com.husain707.reactive.growth.ecom.search.indexer.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Date;

public class CategoryEventPayload {

    @JsonProperty("data")
    private CategoryEventData data;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_timestamp")
    private LocalDateTime eventTimestamp;

    // Getters and Setters
    public CategoryEventData getData() { return data; }
    public void setData(CategoryEventData data) { this.data = data; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}