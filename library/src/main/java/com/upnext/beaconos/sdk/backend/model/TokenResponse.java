/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.upnext.beaconos.sdk.backend.Validable;

public class TokenResponse implements Validable {

    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public long expiresIn;

    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("created_at")
    public long createdAt;

    @Override
    public boolean isValid() {
        // TODO
        return true;
    }
}
