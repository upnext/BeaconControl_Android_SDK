/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents action that may be performed during Beacon SDK usage.
 */
public class Action implements Serializable {

    /**
     * This enum represents type of action.
     */
    public enum Type {
        /**
         * Url action type.
         */
        url,

        /**
         * Coupon action type.
         */
        coupon,

        /**
         * Custom action type.
         */
        custom
    }

    /**
     * Action id.
     */
    @JsonProperty("id")
    public Long id;

    /**
     * Action type.
     */
    @JsonProperty("type")
    public Type type;

    /**
     * Action name.
     */
    @JsonProperty("name")
    public String name;

    /**
     * Action payload.
     */
    @JsonProperty("payload")
    public Payload payload;

    /**
     * This class contains action parameters.
     */
    public static class Payload implements Serializable {

        /**
         * Url for {@link Action.Type#url} action.
         */
        @JsonProperty("url")
        public String url;
    }

    /**
     * List of action's custom attributes.
     */
    @JsonProperty("custom_attributes")
    public List<CustomAttribute> customAttributes;

    /**
     * This class represents custom attribute that may be component of the {@link Action.Type#custom} action.
     */
    public static class CustomAttribute implements Serializable {

        /**
         * Attribute id.
         */
        @JsonProperty("id")
        public long id;

        /**
         * Attribute name.
         */
        @JsonProperty("name")
        public String name;

        /**
         * Attribute value.
         */
        @JsonProperty("value")
        public String value;
    }
}
