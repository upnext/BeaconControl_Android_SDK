/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import io.upnext.beaconcontrol.sdk.backend.model.TokenResponse;

import retrofit2.Call;

public interface BeaconControlTokenManager {

    Call<TokenResponse> getNewTokenCall();

    Call<TokenResponse> refreshTokenCall(TokenResponse token);
}
