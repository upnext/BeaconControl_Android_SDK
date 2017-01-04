/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

public class TokenCredentials {

    private final String clientId;
    private final String clientSecret;
    private final String userId;

    public TokenCredentials(String clientId, String clientSecret, String userId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUserId() {
        return userId;
    }
}
