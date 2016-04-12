/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateEventsRequest {

    @JsonProperty(value = "events")
    public List<Event> events;

    public static class Event {

        public enum EventType {enter, leave}

        @JsonProperty("event_type")
        public EventType eventType;

        @JsonProperty("proximity_id")
        public String proximityId;

        @JsonProperty("zone_id")
        public Long zoneId;

        @JsonProperty("action_id")
        public long actionId;

        @JsonProperty("timestamp")
        public long timestamp;

        public Event() {

        }

        public Event(EventType eventType) {
            this.eventType = eventType;
        }

        public Event setEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Event setProximityId(String proximityId) {
            this.proximityId = proximityId;
            return this;
        }

        public Event setZoneId(Long zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public Event setActionId(long actionId) {
            this.actionId = actionId;
            return this;
        }

        public Event setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }

    public CreateEventsRequest(List<Event> events) {
        this.events = events;
    }
}
