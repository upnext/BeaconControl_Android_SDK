/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class BaseTokenRequest {

    public enum GrantType {password, refresh_token}

    @JsonProperty("client_id")
    public String clientId;

    @JsonProperty("client_secret")
    public String clientSecret;

    @JsonProperty("grant_type")
    public GrantType grantType;

    public BaseTokenRequest(String clientId, String clientSecret, GrantType grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
    }
}
