/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.util;

public class BeaconLocationUtils {

    private static final float MIN_LAT = -90;
    private static final float MAX_LAT = 90;
    private static final float MIN_LNG = -180;
    private static final float MAX_LNG = 180;
    private static final int MIN_FLOOR = 0; // TODO to be confirmed
    private static final int MAX_FLOOR = 10; // TODO to be confirmed

    private BeaconLocationUtils() {
    }

    public static boolean isLatValid(float lat) {
        return lat >= MIN_LAT && lat <= MAX_LAT;
    }

    public static boolean isLngValid(float lng) {
        return lng >= MIN_LNG && lng <= MAX_LNG;
    }

    public static boolean isFloorValid(int floor) {
        return floor >= MIN_FLOOR && floor <= MAX_FLOOR;
    }
}
