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
import com.upnext.beaconos.sdk.backend.model.GetPresenceResponse;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface BeaconControlService {

    @GET("configurations")
    Call<GetConfigurationsResponse> getConfigurations();

    @POST("events")
    Call<Empty> createEvents(@Body CreateEventsRequest createEventsRequest);

    @GET("presence")
    Call<GetPresenceResponse> getPresence(
            @Query("ranges[]") List<Long> ranges,
            @Query("zones[]") List<Long> zones);
}
