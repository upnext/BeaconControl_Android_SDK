/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.service;

import android.content.Intent;

import io.upnext.beaconcontrol.sdk.internal.model.BeaconsList;
import io.upnext.beaconcontrol.sdk.util.ULog;

/**
 * Service responsible for notifying client that new beacon configuration has been just loaded from
 * the backend.
 */
public class BeaconConfigurationChangeProcessor extends BaseProcessor {

    private static final String TAG = BeaconConfigurationChangeProcessor.class.getSimpleName();

    public interface Extra {
        String BEACONS_LIST = "io.upnext.beaconcontrol.sdk.backend.service.BeaconConfigurationChangeProcessor.BEACONS_LIST";
    }

    public BeaconConfigurationChangeProcessor() {
        super("BeaconConfigurationChangeProcessor");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ULog.d(TAG, "onHandleIntent.");
        eventsManager.processConfigurationLoaded((BeaconsList) intent.getSerializableExtra(Extra.BEACONS_LIST));
    }
}