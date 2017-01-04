/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk;

import java.util.List;

/**
 * This interface defines whether actions should be performed automatically or manually and represents listener for actions start and end.
 */
public interface BeaconDelegate {

    /**
     * Defines whether actions should be performed automatically or manually.
     *
     * @return True if actions should be performed automatically, false otherwise.
     */
    boolean shouldPerformActionAutomatically();

    /**
     * Called when action performing begins.
     *
     * @param action Action that has been performed.
     */
    void onActionStart(Action action);

    /**
     * Called when action performing ends.
     *
     * @param action Action that has been performed.
     */
    void onActionEnd(Action action);

    /**
     * Called when beacons configuration has just been fetched from the backend
     * @param beacons List of beacons
     */
    void onBeaconsConfigurationLoaded(List<Beacon> beacons);

    /**
     * Called when a monitored beacon's proximity has changed
     * @param beacon Monitored beacon
     */
    void onBeaconProximityChanged(Beacon beacon);
}
