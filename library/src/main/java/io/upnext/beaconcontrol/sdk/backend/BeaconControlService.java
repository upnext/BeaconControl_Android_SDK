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
import io.upnext.beaconcontrol.sdk.backend.model.GetPresenceResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BeaconControlService {

    @GET("configurations")
    Call<GetConfigurationsResponse> getConfigurations();

    @POST("events")
    Call<Void> createEvents(@Body CreateEventsRequest createEventsRequest);

    @GET("presence")
    Call<GetPresenceResponse> getPresence(
            @Query("ranges[]") List<Long> ranges,
            @Query("zones[]") List<Long> zones);
}
