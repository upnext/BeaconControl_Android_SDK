/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk;

/**
 * This enum represents error codes that may occur during Beacon SDK usage.
 */
public enum ErrorCode {

    /**
     * Server error.
     */
    BEACON_CONTROL_ERROR,

    /**
     * There is some issue with Internet connection or web address.
     */
    IO_ERROR, // check Internet connection and web addresses

    /**
     * Device is not connected to the Internet.
     */
    OFFLINE,  // device not connected to the Internet

    /**
     * Bluetooth low energy feature is not supported.
     */
    BLE_NOT_SUPPORTED,

    /**
     * Bluetooth service is not enabled.
     */
    BLUETOOTH_NOT_ENABLED
}
