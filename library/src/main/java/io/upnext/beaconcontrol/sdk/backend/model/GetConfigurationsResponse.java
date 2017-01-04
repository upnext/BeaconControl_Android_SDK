/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.upnext.beaconcontrol.sdk.Action;
import io.upnext.beaconcontrol.sdk.backend.Validable;
import io.upnext.beaconcontrol.sdk.util.StringUtils;
import io.upnext.beaconcontrol.sdk.util.ULog;

public class GetConfigurationsResponse implements Serializable, Validable {

    private static final String TAG = GetConfigurationsResponse.class.getSimpleName();

    @JsonProperty("extensions")
    public Extensions extensions;

    public static class Extensions implements Serializable, Validable {

        @JsonProperty("presence")
        public Presence presence;

        //may add fields and classes when extra extensions will be present

        public static class Presence implements Serializable {

            @JsonProperty("ranges")
            public List<Long> ranges;

            @JsonProperty("zones")
            public List<Long> zones;
        }

        @Override
        public boolean isValid() {
            // TODO
            return true;
        }
    }

    @JsonProperty("triggers")
    public List<Trigger> triggers;

    public static class Trigger implements Serializable, Validable {

        @JsonProperty("id")
        public long id;

        @JsonProperty("conditions")
        public List<Condition> conditions;

        public static class Condition implements Serializable {

            public enum EventType {enter, leave, dwell_time, immediate, near, far}

            public enum Type {event_type}

            @JsonProperty("event_type")
            public EventType eventType;

            @JsonProperty("type")
            public Type type;
        }

        @JsonProperty("test")
        public Boolean test;

        @JsonProperty("range_ids")
        public List<Long> range_ids;

        @JsonProperty("zone_ids")
        public List<Long> zone_ids;

        @JsonProperty("action")
        public Action action;

        @Override
        public boolean isValid() {
            // TODO
            return true;
        }
    }

    @JsonProperty("ranges")
    public List<Range> ranges;

    public static class Range implements Serializable, Validable {

        public enum Protocol {
            iBeacon, Eddystone, Unknown;

            private static Map<String, Protocol> protocolsMap = new HashMap<>();

            static {
                protocolsMap.put("iBeacon", iBeacon);
                protocolsMap.put("Eddystone", Eddystone);
                protocolsMap.put("Unknown", Unknown);
            }

            @JsonCreator
            public static Protocol forValue(String value) {
                return protocolsMap.containsKey(value) ? protocolsMap.get(value) : Unknown;
            }

            @JsonValue
            public String toValue() {
                for (Map.Entry<String, Protocol> entry : protocolsMap.entrySet()) {
                    if (entry.getValue() == this) {
                        return entry.getKey();
                    }
                }

                return Unknown.name();
            }
        }

        @JsonProperty("id")
        public long id;

        @JsonProperty("name")
        public String name;

        @JsonProperty("protocol")
        public Protocol protocol;

        @JsonProperty("proximity_id")
        public BeaconProximity proximityId;

        @JsonProperty("location")
        public Location location;

        public static class Location implements Serializable {

            @JsonProperty("lat")
            public void setLat(final String latStr) {
                lat = StringUtils.safeParseFloat(latStr, -1);
            }

            public float lat;

            @JsonProperty("lng")
            public void setLng(final String lngStr) {
                lng = StringUtils.safeParseFloat(lngStr, -1);
            }

            public float lng;

            @JsonProperty("floor")
            public int floor;
        }

        @Override
        public boolean isValid() {
            return name != null && !name.isEmpty() &&
                    proximityId != null && proximityId.isValid();
        }
    }

    @JsonProperty("zones")
    public List<Zone> zones;

    public static class Zone implements Serializable, Validable {

        @JsonProperty("id")
        public long id;

        @JsonProperty("name")
        public String name;

        @JsonProperty("beacon_ids")
        public List<Long> beaconIds;

        @JsonProperty("color")
        public BeaconSDKColor color;

        @Override
        public boolean isValid() {
            // TODO
            return true;
        }
    }

    @JsonProperty("ttl")
    public long ttl;

    @Override
    public boolean isValid() {
        if (!extensions.isValid()) {
            ULog.d(TAG, "Configurations extensions are invalid.");
            return false;
        }

        for (Trigger trigger : triggers) {
            if (!trigger.isValid()) {
                ULog.d(TAG, "Configurations triggers are invalid.");
                return false;
            }
        }

        for (Range range : ranges) {
            if (!range.isValid()) {
                ULog.d(TAG, "Configurations ranges are invalid.");
                return false;
            }
        }

        for (Zone zone : zones) {
            if (!zone.isValid()) {
                ULog.d(TAG, "Configurations zones are invalid.");
                return false;
            }
        }

        return true;
    }
}
