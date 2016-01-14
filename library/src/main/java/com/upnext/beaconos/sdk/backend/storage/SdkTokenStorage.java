/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.upnext.beaconos.sdk.backend.model.TokenResponse;

public class SdkTokenStorage {

    private static final String PREFERENCES_STORE_NAME = "SdkTokenStorage.preferences";

    private static final String PROPERTY_ACCESS_TOKEN = "TokenStorage.PROPERTY_ACCESS_TOKEN";
    private static final String PROPERTY_TOKEN_TYPE = "TokenStorage.PROPERTY_TOKEN_TYPE";
    private static final String PROPERTY_EXPIRES_IN = "TokenStorage.PROPERTY_EXPIRES_IN";
    private static final String PROPERTY_REFRESH_TOKEN = "TokenStorage.PROPERTY_REFRESH_TOKEN";
    private static final String PROPERTY_CREATED_AT = "TokenStorage.PROPERTY_CREATED_AT";

    private final SharedPreferences prefs;

    public SdkTokenStorage(Context context) {
        prefs = context.getSharedPreferences(PREFERENCES_STORE_NAME, Context.MODE_PRIVATE);
    }

    public void storeToken(TokenResponse token) {
        prefs.edit()
                .putString(PROPERTY_ACCESS_TOKEN, token.accessToken)
                .putString(PROPERTY_TOKEN_TYPE, token.tokenType)
                .putLong(PROPERTY_EXPIRES_IN, token.expiresIn)
                .putString(PROPERTY_REFRESH_TOKEN, token.refreshToken)
                .putLong(PROPERTY_CREATED_AT, token.createdAt)
                .apply();
    }

    public TokenResponse retrieveToken() {
        if (!prefs.contains(PROPERTY_ACCESS_TOKEN)) {
            return null;
        }
        TokenResponse token = new TokenResponse();
        token.accessToken = prefs.getString(PROPERTY_ACCESS_TOKEN, "");
        token.tokenType = prefs.getString(PROPERTY_TOKEN_TYPE, "");
        token.expiresIn = prefs.getLong(PROPERTY_EXPIRES_IN, -1);
        token.refreshToken = prefs.getString(PROPERTY_REFRESH_TOKEN, "");
        token.createdAt = prefs.getLong(PROPERTY_CREATED_AT, -1);
        return token;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
