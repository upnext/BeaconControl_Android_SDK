/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.upnext.beaconos.sdk.backend.BeaconControlManagerImpl;
import com.upnext.beaconos.sdk.backend.BeaconControlManager;
import com.upnext.beaconos.sdk.backend.events.EventsManager;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.core.BeaconPreferences;
import com.upnext.beaconos.sdk.core.BeaconPreferencesImpl;
import com.upnext.beaconos.sdk.core.Config;
import com.upnext.beaconos.sdk.core.ConfigImpl;
import com.upnext.beaconos.sdk.util.ULog;

public class BeaconEventProcessor extends IntentService {

    private static final String TAG = BeaconEventProcessor.class.getSimpleName();

    public interface Extra {
        String BEACON = "com.upnext.beaconos.sdk.backend.service.BeaconIntentProcessor.BEACON";
        String TRIGGER = "com.upnext.beaconos.sdk.backend.service.BeaconIntentProcessor.TRIGGER";
        String EVENT = "com.upnext.beaconos.sdk.backend.service.BeaconIntentProcessor.EVENT";
    }

    private EventsManager eventsManager;

    public BeaconEventProcessor() {
        super("BeaconIntentProcessor");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context appContext = getApplicationContext();
        Config config = ConfigImpl.getInstance(appContext);
        BeaconPreferences preferences = BeaconPreferencesImpl.getInstance(appContext);
        BeaconControlManager beaconControlManager = BeaconControlManagerImpl.getInstance(appContext, config, preferences);

        eventsManager = EventsManager.getInstance(appContext, config, preferences, beaconControlManager);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ULog.d(TAG, "onHandleIntent.");

        eventsManager.processEvent((BeaconModel) intent.getSerializableExtra(Extra.BEACON),
                (GetConfigurationsResponse.Trigger) intent.getSerializableExtra(Extra.TRIGGER),
                (EventInfo) intent.getSerializableExtra(Extra.EVENT));
    }
}
