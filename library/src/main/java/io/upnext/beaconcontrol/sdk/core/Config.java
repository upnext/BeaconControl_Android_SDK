/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.core;

public interface Config {

    int getConnectionTimeoutInSec();

    int getReadTimeoutInSec();

    int getWriteTimeoutInSec();

    String getServiceBaseUrl();

    int getMaxRunningServices();

    int getLeaveMsgDelayInMillis();

    String getBeaconParserLayout();

    int getForegroundScanDurationInMillis();

    int getForegroundPauseDurationInMillis();

    int getBackgroundScanDurationInMillis();

    int getBackgroundPauseDurationInMillis();

    long getEventsSendoutDelayInMillis();
}
