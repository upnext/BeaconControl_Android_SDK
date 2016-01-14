/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.util;

import android.text.TextUtils;

public final class StringUtils {

    private static final String TAG = StringUtils.class.getSimpleName();

    public static final String EMPTY = "";

    private StringUtils() {
    }

    public static String capitalizeFirstLetter(String input) {
        if (TextUtils.isEmpty(input)) {
            return input;
        }

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static float safeParseFloat(String str, float fallback) {
        if (str == null) {
            // this check is needed for proper deserialization
            return fallback;
        }

        try {
            return Float.valueOf(str);
        } catch (NumberFormatException e) {
            ULog.d(TAG, String.format("Cannot parse float from %s.", str));
            return fallback;
        }
    }
}
