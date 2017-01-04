/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.upnext.beaconcontrol.sdk.backend.Validable;

import java.util.List;
import java.util.Map;

public class GetPresenceResponse implements Validable {

    @JsonProperty("ranges")
    public Map<String, List<String>> ranges;

    @JsonProperty("zones")
    public Map<String, List<String>> zones;

    @Override
    public boolean isValid() {
        // TODO
        return true;
    }
}
