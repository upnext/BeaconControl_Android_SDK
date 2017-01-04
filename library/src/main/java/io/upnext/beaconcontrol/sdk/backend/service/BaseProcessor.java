/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.service;

import android.app.IntentService;
import android.content.Context;

import io.upnext.beaconcontrol.sdk.backend.BeaconControlManager;
import io.upnext.beaconcontrol.sdk.backend.BeaconControlManagerImpl;
import io.upnext.beaconcontrol.sdk.backend.events.EventsManager;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferences;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferencesImpl;
import io.upnext.beaconcontrol.sdk.core.Config;
import io.upnext.beaconcontrol.sdk.core.ConfigImpl;

public abstract class BaseProcessor extends IntentService {

    private static final String TAG = BaseProcessor.class.getSimpleName();

    protected EventsManager eventsManager;

    public BaseProcessor(String serviceName) {
        super("BaseProcessor");
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
}