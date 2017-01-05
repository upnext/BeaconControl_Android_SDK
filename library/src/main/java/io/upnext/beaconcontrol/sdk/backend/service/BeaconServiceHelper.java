/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.upnext.beaconcontrol.sdk.BeaconControl;
import io.upnext.beaconcontrol.sdk.ErrorCode;
import io.upnext.beaconcontrol.sdk.backend.BeaconControlManager;
import io.upnext.beaconcontrol.sdk.backend.HttpListener;
import io.upnext.beaconcontrol.sdk.backend.mediator.GetConfigurationsCallMediator;
import io.upnext.beaconcontrol.sdk.backend.mediator.HttpCallMediator;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse;
import io.upnext.beaconcontrol.sdk.core.Config;
import io.upnext.beaconcontrol.sdk.util.ApplicationUtils;
import io.upnext.beaconcontrol.sdk.util.ULog;

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
                handleUnsupportedProtocols(response);

                configurations = response;

                if (bound) {
                    notifyService(getBeaconServiceIntentWithConf(), BeaconService.Command.START_SCAN);
                }
            }

            @Override
            public void onError(ErrorCode errorCode, Throwable t) {
                ULog.e(TAG, "Error in getConfigurations task, " + errorCode.name(), t);

                BeaconControl.onError(errorCode);
            }

            @Override
            public void onEnd() {
                getConfigurationsCallMediator = null;
            }
        });
        getConfigurationsCallMediator.getConfigurations();
    }

    private void handleUnsupportedProtocols(GetConfigurationsResponse response) {
        List<Long> unsupportedRangeIds = removeRangesWithUnsupportedProtocols(response);
        removeUnsupportedRangesFromTriggers(response, unsupportedRangeIds);
    }

    private List<Long> removeRangesWithUnsupportedProtocols(GetConfigurationsResponse response) {
        List<Long> unsupportedRangeIds = new ArrayList<>();
        Iterator<GetConfigurationsResponse.Range> it = response.ranges.iterator();
        while (it.hasNext()) {
            GetConfigurationsResponse.Range range = it.next();
            switch (range.protocol) {
                case iBeacon:
                    // ignore, as this protocol is supported
                    break;
                default:
                    ULog.w(TAG, "Unsupported protocol for range id " + range.id);
                    unsupportedRangeIds.add(range.id);
                    it.remove();
            }
        }
        return unsupportedRangeIds;
    }

    private void removeUnsupportedRangesFromTriggers(GetConfigurationsResponse response, List<Long> unsupportedRangeIds) {
        Iterator<GetConfigurationsResponse.Trigger> it = response.triggers.iterator();
        while (it.hasNext()) {
            GetConfigurationsResponse.Trigger trigger = it.next();
            trigger.range_ids.removeAll(unsupportedRangeIds);

            if (isTriggerUnused(trigger)) {
                it.remove();
            }
        }
    }

    private boolean isTriggerUnused(GetConfigurationsResponse.Trigger trigger) {
        return trigger.range_ids.isEmpty();
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

        configurations = null;
        beaconControlManager.clearToken();
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
