/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import io.upnext.beaconcontrol.sdk.backend.model.CreateEventsRequest;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse;
import io.upnext.beaconcontrol.sdk.backend.model.GetPresenceRequest;
import io.upnext.beaconcontrol.sdk.backend.model.GetPresenceResponse;

import retrofit2.Call;

public interface BeaconControlManager {

    Call<GetConfigurationsResponse> getConfigurationsCall();

    Call<Void> createEventsCall(CreateEventsRequest request);

    Call<GetPresenceResponse> getPresenceCall(GetPresenceRequest request);
}
