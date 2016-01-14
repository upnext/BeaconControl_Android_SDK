/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend;

import com.upnext.beaconos.sdk.backend.model.CreateEventsRequest;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.backend.model.Empty;
import com.upnext.beaconos.sdk.backend.model.GetPresenceRequest;
import com.upnext.beaconos.sdk.backend.model.GetPresenceResponse;

import retrofit.Call;

public interface BeaconControlManager {

    Call<GetConfigurationsResponse> getConfigurationsCall();

    Call<Empty> createEventsCall(CreateEventsRequest request);

    Call<GetPresenceResponse> getPresenceCall(GetPresenceRequest request);
}
