/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import android.content.Context;
import android.text.format.DateUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.upnext.beaconcontrol.sdk.backend.model.CreateEventsRequest;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse;
import io.upnext.beaconcontrol.sdk.backend.model.GetPresenceRequest;
import io.upnext.beaconcontrol.sdk.backend.model.GetPresenceResponse;
import io.upnext.beaconcontrol.sdk.backend.model.TokenResponse;
import io.upnext.beaconcontrol.sdk.backend.storage.SdkTokenStorage;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferences;
import io.upnext.beaconcontrol.sdk.core.Config;
import io.upnext.beaconcontrol.sdk.util.StringUtils;
import io.upnext.beaconcontrol.sdk.util.ULog;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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

    private final SdkTokenStorage tokenStorage;
    private final BeaconControlService beaconControlService;
    private final BeaconControlTokenManager beaconControlOAuthManager;

    private BeaconControlManagerImpl(Context context, Config config, BeaconPreferences preferences) {
        tokenStorage = new SdkTokenStorage(context);

        ObjectMapper objectMapper = getObjectMapper();

        OkHttpClient okHttpClient = getHttpClientBuilder(config)
                .addNetworkInterceptor(new BeaconControlInterceptor())
                .build();
        beaconControlService = createService(getRetrofitInstance(config.getServiceBaseUrl(), objectMapper, okHttpClient), BeaconControlService.class);

        beaconControlOAuthManager = new BeaconControlTokenManagerImpl(preferences,
                createService(getRetrofitInstance(config.getServiceBaseUrl(), objectMapper, getHttpClientBuilder(config).build()), BeaconControlTokenService.class));
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }

    private OkHttpClient.Builder getHttpClientBuilder(Config config) {
        return new OkHttpClient.Builder()
                .connectTimeout(config.getConnectionTimeoutInSec(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeoutInSec(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeoutInSec(), TimeUnit.SECONDS);
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
            //TODO handle redirects with okhttp as currently if a 302 is happening, the access token will be added to first request (the one returning 302)
            //but not to the redirected one
            TokenResponse token = tokenStorage.retrieveToken();

            if (token == null) {
                token = checkErrorAndGetTokenResponse(beaconControlOAuthManager.getNewTokenCall());
            } else if (tokenIsInvalid(token)) {
                token = checkErrorAndGetTokenResponse(beaconControlOAuthManager.refreshTokenCall(token));
            }

            if (token == null) {
                ULog.w(TAG, "Token has not been retrieved.");
                //proceed without token and let it fail, null was causing exception
                return chain.proceed(chain.request());
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
        if (token != null) {
            storeToken(token);
        }
        return token;
    }

    private TokenResponse checkErrorAndGetTokenResponse(Call<TokenResponse> call) {
        try {
            return storeTokenAndGetTokenResponse(call.execute().body());
        } catch (IOException e) {
            ULog.e(TAG, "Error in getToken task.", e);
            return null;
        }
    }

    @Override
    public Call<GetConfigurationsResponse> getConfigurationsCall() {
        return beaconControlService.getConfigurations();
    }

    @Override
    public Call<Void> createEventsCall(CreateEventsRequest request) {
        return beaconControlService.createEvents(request);
    }

    @Override
    public Call<GetPresenceResponse> getPresenceCall(GetPresenceRequest request) {
        return beaconControlService.getPresence(request.getRanges(), request.getZones());
    }
}