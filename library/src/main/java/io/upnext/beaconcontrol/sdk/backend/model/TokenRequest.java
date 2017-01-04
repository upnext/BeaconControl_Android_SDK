/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenRequest extends BaseTokenRequest {

    public enum OS {android}

    public enum Environment {production, sandbox}

    @JsonProperty("user_id")
    public String userId;

    @JsonProperty("os")
    public OS os;

    @JsonProperty("environment")
    public Environment environment;

    @JsonProperty("push_token")
    public String pushToken;

    public TokenRequest(String clientId, String clientSecret, GrantType grantType, String userId, OS os, Environment environment, String pushToken) {
        super(clientId, clientSecret, grantType);
        this.userId = userId;
        this.os = os;
        this.environment = environment;
        this.pushToken = pushToken;
    }
}
