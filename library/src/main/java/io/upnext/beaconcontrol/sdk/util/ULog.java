/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.util;

import android.util.Log;

public class ULog {

    private static boolean enabled = false;

    private static String format(String tag) {
        return "BeaconControl~" + tag;
    }

    public static void enableLogging(boolean enable) {
        enabled = enable;
    }

    public static void d(String tag, String msg) {
        if (enabled) {
            Log.d(format(tag), msg);
        }
    }

    public static void w(String tag, String msg) {
        if (enabled) {
            Log.w(format(tag), msg);
        }
    }

    public static void e(String tag, String msg) {
        if (enabled) {
            Log.e(format(tag), msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (enabled) {
            Log.e(format(tag), msg, tr);
        }
    }
}
