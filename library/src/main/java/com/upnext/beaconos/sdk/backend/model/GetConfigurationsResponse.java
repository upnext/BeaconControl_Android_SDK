/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.upnext.beaconos.sdk.Action;
import com.upnext.beaconos.sdk.backend.Validable;
import com.upnext.beaconos.sdk.util.BeaconLocationUtils;
import com.upnext.beaconos.sdk.util.StringUtils;
import com.upnext.beaconos.sdk.util.ULog;

import java.io.Serializable;
import java.util.List;

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

        @JsonProperty("id")
        public long id;

        @JsonProperty("name")
        public String name;

        @JsonProperty("proximity_id")
        public BeaconProximity proximityId;

        @JsonProperty("location")
        public Location location;

        public static class Location implements Serializable, Validable {

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

            @Override
            public boolean isValid() {
                return lat != -1 && BeaconLocationUtils.isLatValid(lat) && lng != -1 && BeaconLocationUtils.isLngValid(lng) && BeaconLocationUtils.isFloorValid(floor);
            }
        }

        @Override
        public boolean isValid() {
            return  name != null && !name.isEmpty() &&
                    proximityId != null && proximityId.isValid() &&
                    location != null && location.isValid();
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
