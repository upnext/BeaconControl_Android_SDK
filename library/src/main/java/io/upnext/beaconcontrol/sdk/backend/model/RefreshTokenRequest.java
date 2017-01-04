/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshTokenRequest extends BaseTokenRequest {

    @JsonProperty("refresh_token")
    public String refreshToken;

    public RefreshTokenRequest(String clientId, String clientSecret, GrantType grantType, String refreshToken) {
        super(clientId, clientSecret, grantType);
        this.refreshToken = refreshToken;
    }
}
