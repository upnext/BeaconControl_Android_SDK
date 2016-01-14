/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend;

import android.content.Context;
import android.text.format.DateUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.upnext.beaconos.sdk.backend.model.CreateEventsRequest;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.backend.model.Empty;
import com.upnext.beaconos.sdk.backend.model.GetPresenceResponse;
import com.upnext.beaconos.sdk.backend.model.GetPresenceRequest;
import com.upnext.beaconos.sdk.backend.model.TokenResponse;
import com.upnext.beaconos.sdk.backend.storage.SdkTokenStorage;
import com.upnext.beaconos.sdk.core.BeaconPreferences;
import com.upnext.beaconos.sdk.core.Config;
import com.upnext.beaconos.sdk.util.ULog;
import com.upnext.beaconos.sdk.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Retrofit;

public class BeaconControlManagerImpl implements BeaconControlManager {

    private static final String TAG = BeaconControlManagerImpl.class.getSimpleName();

    private static final int REFRESH_TOKEN_STRATEGY_NOT_USED = 0;

    private static BeaconControlManagerImpl sInstance;

    public static synchronized BeaconControlManagerImpl getInstance(Context context, Config config, BeaconPreferences preferences) {
        if (sInstance == null) {
            sInstance = new BeaconControlManagerImpl(context, config, preferences);
        }
        return sInstance;
    }

    private final Context context;
    private final SdkTokenStorage tokenStorage;
    private final BeaconControlService beaconControlService;
    private final BeaconControlTokenManager beaconControlOAuthManager;

    private BeaconControlManagerImpl(Context context, Config config, BeaconPreferences preferences) {
        this.context = context;

        tokenStorage = new SdkTokenStorage(context);

        ObjectMapper objectMapper = getObjectMapper();

        OkHttpClient okHttpClient = getHttpClient(config);
        okHttpClient.interceptors().add(new BeaconControlInterceptor());
        beaconControlService = createService(getRetrofitInstance(config.getServiceBaseUrl(), objectMapper, okHttpClient), BeaconControlService.class);

        beaconControlOAuthManager = new BeaconControlTokenManagerImpl(preferences,
                createService(getRetrofitInstance(config.getServiceBaseUrl(), objectMapper, getHttpClient(config)), BeaconControlTokenService.class));
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }

    private OkHttpClient getHttpClient(Config config) {
        OkHttpClient httpClient = new OkHttpClient();

        httpClient.setConnectTimeout(config.getConnectionTimeoutInSec(), TimeUnit.SECONDS);
        httpClient.setReadTimeout(config.getReadTimeoutInSec(), TimeUnit.SECONDS);
        httpClient.setWriteTimeout(config.getWriteTimeoutInSec(), TimeUnit.SECONDS);

        return httpClient;
    }

    private Retrofit getRetrofitInstance(String serviceBaseUrl, ObjectMapper objectMapper, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(serviceBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build();
    }

    private <T> T createService(Retrofit retrofit, Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    private class BeaconControlInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            TokenResponse token = tokenStorage.retrieveToken();

            if (token == null) {
                token = checkErrorAndGetTokenResponse(beaconControlOAuthManager.getNewTokenCall());
            } else if (tokenIsInvalid(token)) {
                token = checkErrorAndGetTokenResponse(beaconControlOAuthManager.refreshTokenCall(token));
            }

            if (token == null) {
                ULog.w(TAG, "Token has not been retrieved.");
                return null;
            }

            return chain.proceed(getRequestWithHeaders(chain.request(), token));
        }

        private Request getRequestWithHeaders(Request originalRequest, TokenResponse token) {
            Request.Builder newRequestBuilder = originalRequest.newBuilder();

            String authHeaderValue = StringUtils.capitalizeFirstLetter(token.tokenType) + " " + token.accessToken;
            newRequestBuilder.addHeader(BeaconControlTokenService.HEADER_AUTHORIZATION, authHeaderValue);

            return newRequestBuilder.build();
        }

        private boolean tokenIsInvalid(TokenResponse token) {
            if (token.createdAt == REFRESH_TOKEN_STRATEGY_NOT_USED || token.expiresIn == REFRESH_TOKEN_STRATEGY_NOT_USED) {
                ULog.d(TAG, "Refresh token strategy not used.");
                return false;
            }

            long nowInSeconds = System.currentTimeMillis() / DateUtils.SECOND_IN_MILLIS;
            return nowInSeconds > (token.createdAt + token.expiresIn);
        }
    }

    private void storeToken(TokenResponse token) {
        tokenStorage.storeToken(token);
    }

    private TokenResponse storeTokenAndGetTokenResponse(TokenResponse token) {
        storeToken(token);
        return token;
    }

    private TokenResponse checkErrorAndGetTokenResponse(Call<TokenResponse> call) {
        try {
            return storeTokenAndGetTokenResponse(call.execute().body());
        } catch (IOException e) {
            ULog.d(TAG, "Error in getToken task.");
            return null;
        }
    }

    @Override
    public Call<GetConfigurationsResponse> getConfigurationsCall() {
        return beaconControlService.getConfigurations();
    }

    @Override
    public Call<Empty> createEventsCall(CreateEventsRequest request) {
        return beaconControlService.createEvents(request);
    }

    @Override
    public Call<GetPresenceResponse> getPresenceCall(GetPresenceRequest request) {
        return beaconControlService.getPresence(request.getRanges(), request.getZones());
    }
}