/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import io.upnext.beaconcontrol.sdk.backend.model.TokenCredentials;
import io.upnext.beaconcontrol.sdk.backend.model.RefreshTokenRequest;
import io.upnext.beaconcontrol.sdk.backend.model.TokenRequest;
import io.upnext.beaconcontrol.sdk.backend.model.TokenResponse;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferences;
import io.upnext.beaconcontrol.sdk.util.StringUtils;

import retrofit2.Call;

public class BeaconControlTokenManagerImpl implements BeaconControlTokenManager {

    private final BeaconPreferences preferences;
    private final BeaconControlTokenService oauthService;

    public BeaconControlTokenManagerImpl(BeaconPreferences preferences, BeaconControlTokenService oauthService) {
        this.preferences = preferences;
        this.oauthService = oauthService;
    }

    @Override
    public Call<TokenResponse> getNewTokenCall() {
        TokenCredentials tokenCredentials = preferences.getOAuthCredentials();

        TokenRequest tokenRequest = new TokenRequest(
                tokenCredentials.getClientId(),
                tokenCredentials.getClientSecret(),
                TokenRequest.GrantType.password,
                tokenCredentials.getUserId(),
                TokenRequest.OS.android,
                TokenRequest.Environment.sandbox,
                StringUtils.EMPTY
        );

        return oauthService.getToken(tokenRequest);
    }

    @Override
    public Call<TokenResponse> refreshTokenCall(TokenResponse token) {
        TokenCredentials tokenCredentials = preferences.getOAuthCredentials();

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(
                tokenCredentials.getClientId(),
                tokenCredentials.getClientSecret(),
                RefreshTokenRequest.GrantType.refresh_token,
                token.refreshToken
        );

        return oauthService.refreshToken(refreshTokenRequest);
    }
}