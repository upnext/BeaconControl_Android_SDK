/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import io.upnext.beaconcontrol.sdk.backend.model.RefreshTokenRequest;
import io.upnext.beaconcontrol.sdk.backend.model.TokenRequest;
import io.upnext.beaconcontrol.sdk.backend.model.TokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BeaconControlTokenService {

    String HEADER_AUTHORIZATION = "Authorization";

    @POST("oauth/token")
    Call<TokenResponse> getToken(@Body TokenRequest tokenRequest);

    @POST("oauth/token")
    Call<TokenResponse> refreshToken(@Body RefreshTokenRequest tokenRequest);
}
