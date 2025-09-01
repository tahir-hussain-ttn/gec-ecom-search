package com.husain707.reactive.growth.ecom.search.indexer.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class BrandUpdateEvent {

    @JsonProperty("data")
    private BrandData data;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_timestamp")
    private Date eventTimestamp;

    // Getters and Setters
    public BrandData getData() { return data; }
    public void setData(BrandData data) { this.data = data; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Date getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Date eventTimestamp) { this.eventTimestamp = eventTimestamp; }
}