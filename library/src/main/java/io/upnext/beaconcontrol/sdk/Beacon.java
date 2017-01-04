/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk;

import java.io.Serializable;

public final class Beacon implements Serializable {

    public static final double DISTANCE_UNDEFINED = Double.MAX_VALUE;

    public enum Proximity {
        OUT_OF_RANGE, FAR, NEAR, IMMEDIATE
    }

    public final Long id;
    public final String name;
    public final String uuid;
    public final Integer major;
    public final Integer minor;
    public Proximity currentProximity;
    public double distance;

    public Beacon(Long id, String name, String uuid, Integer major, Integer minor, Proximity currentProximity, double distance) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.currentProximity = currentProximity;
        this.distance = distance;
    }
}
