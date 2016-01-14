/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.util;

import java.util.Date;
import java.util.Random;

public class RandUtils {

    private RandUtils() {
    }

    private static Random sInstance = new Random(new Date().getTime());

    public static long nextLong() {
        return sInstance.nextLong();
    }
}
