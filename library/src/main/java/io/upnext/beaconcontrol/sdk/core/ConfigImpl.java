/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.core;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;

import io.upnext.beaconcontrol.sdk.R;

public class ConfigImpl implements Config {

    private static final Object sInstanceLock = new Object();
    private static Config sInstance;

    public static Config getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new ConfigImpl(context);
            }
            return sInstance;
        }
    }

    private Resources resources;

    private ConfigImpl(Context context) {
        this.resources = context.getResources();
    }

    @Override
    public int getConnectionTimeoutInSec() {
        return resources.getInteger(R.integer.sdk_config__connect_timeout_seconds);
    }

    @Override
    public int getReadTimeoutInSec() {
        return resources.getInteger(R.integer.sdk_config__read_timeout_seconds);
    }

    @Override
    public int getWriteTimeoutInSec() {
        return resources.getInteger(R.integer.sdk_config__write_timeout_seconds);
    }

    @Override
    public String getServiceBaseUrl() {
        return resources.getString(R.string.sdk_config__service_base_url);
    }

    @Override
    public int getMaxRunningServices() {
        return resources.getInteger(R.integer.sdk_config__max_running_services);
    }

    @Override
    public int getLeaveMsgDelayInMillis() {
        return resources.getInteger(R.integer.sdk_config__leave_msg_delay_millis);
    }

    @Override
    public String getBeaconParserLayout() {
        return resources.getString(R.string.sdk_config__iBeacon_parser_layout);
    }

    @Override
    public int getForegroundScanDurationInMillis() {
        return resources.getInteger(R.integer.sdk_config__foreground_scan_duration_millis);
    }

    @Override
    public int getForegroundPauseDurationInMillis() {
        return resources.getInteger(R.integer.sdk_config__foreground_pause_duration_millis);
    }

    @Override
    public int getBackgroundScanDurationInMillis() {
        return resources.getInteger(R.integer.sdk_config__background_scan_duration_millis);
    }

    @Override
    public int getBackgroundPauseDurationInMillis() {
        return resources.getInteger(R.integer.sdk_config__background_pause_duration_millis);
    }

    @Override
    public long getEventsSendoutDelayInMillis() {
        return resources.getInteger(R.integer.sdk_config__events_sendout_timeout_seconds) * DateUtils.SECOND_IN_MILLIS;
    }
}
