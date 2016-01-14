/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.upnext.beaconos.sdk.BeaconSDK;
import com.upnext.beaconos.sdk.ErrorCode;
import com.upnext.beaconos.sdk.backend.HttpListener;
import com.upnext.beaconos.sdk.backend.BeaconControlManager;
import com.upnext.beaconos.sdk.backend.mediator.GetConfigurationsCallMediator;
import com.upnext.beaconos.sdk.backend.mediator.HttpCallMediator;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.core.Config;
import com.upnext.beaconos.sdk.util.ApplicationUtils;
import com.upnext.beaconos.sdk.util.ULog;

public class BeaconServiceHelper {

    private static final String TAG = BeaconServiceHelper.class.getSimpleName();

    private static final Object sInstanceLock = new Object();
    private static BeaconServiceHelper sInstance;

    private final Context context;
    private final Config config;
    private final BeaconControlManager beaconControlManager;

    private GetConfigurationsCallMediator getConfigurationsCallMediator;
    private GetConfigurationsResponse configurations;

    private boolean bound = false;

    private BeaconServiceHelper(Context context, Config config, BeaconControlManager beaconControlManager) {
        this.context = context;
        this.config = config;
        this.beaconControlManager = beaconControlManager;
    }

    public static BeaconServiceHelper getInstance(Context context, Config config, BeaconControlManager beaconControlManager) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new BeaconServiceHelper(context, config, beaconControlManager);
            }
            return sInstance;
        }
    }

    public void getConfigurationsAsync() {
        getConfigurationsCallMediator = new GetConfigurationsCallMediator(context, beaconControlManager, new HttpListener<GetConfigurationsResponse>() {
            @Override
            public void onSuccess(GetConfigurationsResponse response) {
                configurations = response;

                if (bound) notifyService(getBeaconServiceIntentWithConf(), BeaconService.Command.START_SCAN);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                ULog.d(TAG, "Error in getConfigurations task, " + errorCode.name());

                BeaconSDK.onError(errorCode);
            }

            @Override
            public void onEnd() {
                getConfigurationsCallMediator = null;
            }
        });
        getConfigurationsCallMediator.getConfigurations();
    }

    private boolean isGetConfigurationsInProgress() {
        return getConfigurationsCallMediator != null;
    }

    public void startScan() {
        if (!isGetConfigurationsInProgress()) {
            getConfigurationsAsync();
        }

        if (bound) return;

        notifyService(getBeaconServiceIntentWithConf(), BeaconService.Command.START_SCAN);
        bound = true;
    }

    public void stopScan() {
        HttpCallMediator.cancelHttpMediator(getConfigurationsCallMediator);

        if (bound) {
            notifyService(getBeaconServiceIntent(), BeaconService.Command.STOP_SCAN);
            bound = false;
        }
    }

    private void notifyService(Intent i, BeaconService.Command cmd) {
        i.putExtra(BeaconService.Extra.COMMAND, cmd);
        context.startService(i);
    }

    private Intent getBeaconServiceIntentWithConf() {
        return getBeaconServiceIntent().putExtra(BeaconService.Extra.CONFIGURATIONS, configurations);
    }

    private Intent getBeaconServiceIntent() {
        Intent i = new Intent(BeaconService.ACTION_NAME);
        i.setComponent(getAppropriateBeaconService(i));
        i.putExtra(BeaconService.Extra.CLIENT_APP_PACKAGE, context.getPackageName());

        return i;
    }

    private ComponentName getAppropriateBeaconService(Intent i) {
        return ApplicationUtils.getAppropriateBeaconService(context, config, i);
    }
}
