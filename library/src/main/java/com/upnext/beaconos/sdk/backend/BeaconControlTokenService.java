/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend;

import com.upnext.beaconos.sdk.backend.model.RefreshTokenRequest;
import com.upnext.beaconos.sdk.backend.model.TokenRequest;
import com.upnext.beaconos.sdk.backend.model.TokenResponse;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

public interface BeaconControlTokenService {

    String HEADER_AUTHORIZATION = "Authorization";

    @POST("oauth/token")
    Call<TokenResponse> getToken(@Body TokenRequest tokenRequest);

    @POST("oauth/token")
    Call<TokenResponse> refreshToken(@Body RefreshTokenRequest tokenRequest);
}
