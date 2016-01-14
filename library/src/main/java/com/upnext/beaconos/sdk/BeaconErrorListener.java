/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk;

/**
 * This interface represents listener for error that may occur during Beacon SDK usage.
 */
public interface BeaconErrorListener {

    /**
     * It is called when an error has occurred.
     *
     * @param errorCode Error code.
     */
    void onError(ErrorCode errorCode);
}
