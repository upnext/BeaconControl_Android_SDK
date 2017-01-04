/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import java.util.List;

public class GetPresenceRequest {

    private final List<Long> ranges;
    private final List<Long> zones;

    public GetPresenceRequest(List<Long> ranges, List<Long> zones) {
        this.ranges = ranges;
        this.zones = zones;
    }

    public List<Long> getRanges() {
        return ranges;
    }

    public List<Long> getZones() {
        return zones;
    }
}
