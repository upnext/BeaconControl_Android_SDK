/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;

import com.upnext.beaconos.sdk.backend.service.BeaconService;
import com.upnext.beaconos.sdk.core.Config;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ApplicationUtils {

    private ApplicationUtils() {
    }

    private static ComponentName getRunningBeaconService(Context context, Config config) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services =
                am.getRunningServices(config.getMaxRunningServices());

        Iterator<ActivityManager.RunningServiceInfo> it = services.iterator();
        while (it.hasNext()) {
            ActivityManager.RunningServiceInfo rsi = it.next();
            if (rsi.service.getClassName().equals(BeaconService.class.getName())) {
                return rsi.service;
            }
        }
        return null;
    }

    public static ComponentName getAppropriateBeaconService(final Context context, Config config, Intent i) {
        ComponentName cn = getRunningBeaconService(context, config);
        if (cn != null) {
            return cn;
        }

        List<ResolveInfo> infos = context.getPackageManager().queryIntentServices(i, Context.BIND_AUTO_CREATE);

        if (infos.isEmpty()) return null;

        Collections.sort(infos, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo l, ResolveInfo r) {
                String lLabel = l.loadLabel(context.getPackageManager()).toString();
                String rLabel = r.loadLabel(context.getPackageManager()).toString();
                return lLabel.compareTo(rLabel);
            }
        });

        ServiceInfo si = infos.get(0).serviceInfo;
        return new ComponentName(si.packageName, si.name);
    }
}
