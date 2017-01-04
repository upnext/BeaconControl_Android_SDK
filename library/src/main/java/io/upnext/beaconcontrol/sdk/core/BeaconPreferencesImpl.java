/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.upnext.beaconcontrol.sdk.backend.model.CreateEventsRequest;
import io.upnext.beaconcontrol.sdk.backend.model.TokenCredentials;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BeaconPreferencesImpl implements BeaconPreferences {

    private static final String TAG = BeaconPreferencesImpl.class.getSimpleName();

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CLIENT_SECRET = "CLIENT_SECRET";
    private static final String USER_ID = "USER_ID";
    private static final String BEACON_PREFERENCES_FILE_NAME = "BEACON_PREFERENCES";
    private static final String LAST_EVENTS_SENDOUT = "LAST_EVENTS_SENDOUT";
    private static final String EVENTS_LIST = "EVENTS_LIST";

    private static final Object sInstanceLock = new Object();
    private static BeaconPreferences sInstance;

    public static BeaconPreferences getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new BeaconPreferencesImpl(context);
            }
            return sInstance;
        }
    }

    private SharedPreferences preferences;
    private ObjectMapper objectMapper;

    private BeaconPreferencesImpl(Context context) {
        preferences = context.getSharedPreferences(BEACON_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        objectMapper = new ObjectMapper();

        setDefaultValues();
    }

    private void setDefaultValues() {
        setEventsSentTimestamp(System.currentTimeMillis());
        setEventsList(new LinkedList<CreateEventsRequest.Event>());
    }

    public void setClientId(String clientId) {
        preferences.edit().putString(CLIENT_ID, clientId).apply();
    }

    public String getClientId() {
        return preferences.getString(CLIENT_ID, "");
    }

    public void setClientSecret(String clientSecret) {
        preferences.edit().putString(CLIENT_SECRET, clientSecret).apply();
    }

    public String getClientSecret() {
        return preferences.getString(CLIENT_SECRET, "");
    }

    public void setUserId(String userId) {
        preferences.edit().putString(USER_ID, userId).apply();
    }

    public String getUserId() {
        return preferences.getString(USER_ID, "");
    }

    @Override
    public void setOAuthCredentials(TokenCredentials tokenCredentials) {
        setClientId(tokenCredentials.getClientId());
        setClientSecret(tokenCredentials.getClientSecret());
        setUserId(tokenCredentials.getUserId());
    }

    @Override
    public TokenCredentials getOAuthCredentials() {
        return new TokenCredentials(getClientId(), getClientSecret(), getUserId());
    }

    @Override
    public void setEventsSentTimestamp(long timestamp) {
        preferences.edit().putLong(LAST_EVENTS_SENDOUT, timestamp).apply();
    }

    @Override
    public long getEventsSentTimestamp() {
        return preferences.getLong(LAST_EVENTS_SENDOUT, 0);
    }

    @Override
    public void setEventsList(List<CreateEventsRequest.Event> events) {
        try {
            String value = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(events);
            preferences.edit().putString(EVENTS_LIST, value).apply();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(TAG +  ", cannot convert events list to string.");
        }
    }

    @Override
    public List<CreateEventsRequest.Event> getEventsList() {
        try {
            return objectMapper.readValue(preferences.getString(EVENTS_LIST, null), new TypeReference<List<CreateEventsRequest.Event>>() {});
        } catch (IOException e) {
            throw new RuntimeException(TAG + ", cannot convert string to events list.");
        }
    }
}
