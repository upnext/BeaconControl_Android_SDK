/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;

import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.core.Config;
import com.upnext.beaconos.sdk.core.ConfigImpl;
import com.upnext.beaconos.sdk.util.ULog;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BeaconService extends Service implements BeaconConsumer {

    private static final String TAG = BeaconService.class.getSimpleName();

    public static final String ACTION_NAME = "com.upnext.beaconos.sdk.BeaconService.ACTION";

    public interface Extra {
        String CLIENT_APP_PACKAGE = "com.upnext.beaconos.sdk.backend.service.BeaconService.CLIENT_APP_PACKAGE";
        String COMMAND = "com.upnext.beaconos.sdk.backend.service.BeaconService.COMMAND";
        String CONFIGURATIONS = "com.upnext.beaconos.sdk.backend.service.BeaconService.CONFIGURATIONS";
    }

    public enum Command {
        START_SCAN,
        STOP_SCAN
    }

    private Config config;
    private ClientsManager clientsManager;
    private BeaconManager beaconManager;

    private EnterLeaveDelayedHandler mEnterLeaveHandler = new EnterLeaveDelayedHandler();
    private Set<String> regionsToLeave = new HashSet<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        Command cmd = (Command) intent.getSerializableExtra(Extra.COMMAND);
        String clientPackageName = intent.getStringExtra(Extra.CLIENT_APP_PACKAGE);

        switch (cmd) {
            case START_SCAN:
                ULog.d(TAG, "Start scan.");
                addClient(clientPackageName, (GetConfigurationsResponse) intent.getSerializableExtra(Extra.CONFIGURATIONS));
                break;
            case STOP_SCAN:
                ULog.d(TAG, "Stop scan.");
                removeClient(clientPackageName);
                break;
            default:
                ULog.d(TAG, "onStartCommand: unknown command.");
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        config = ConfigImpl.getInstance(getApplicationContext());

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(config.getBeaconParserLayout()));
        beaconManager.bind(this);

        clientsManager = new ClientsManager(this, beaconManager);
    }

    private void addClient(String packageName, GetConfigurationsResponse conf) {
        if (packageName == null) {
            ULog.d(TAG, "Cannot add client, packageName is null.");
            return;
        }
        if (conf == null) {
            ULog.d(TAG, "Cannot add client, conf is null.");
            return;
        }

        if (clientsManager.containsClient(packageName)) {
            clientsManager.updateClient(packageName, conf);
        } else {
            clientsManager.addClient(packageName, conf);
        }
    }

    private void removeClient(String packageName) {
        clientsManager.removeClient(packageName);
        if (!clientsManager.hasClients()) {
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        ULog.d(TAG, "onBeaconServiceConnect");
        
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                sendDelayedEnter(region);
            }

            @Override
            public void didExitRegion(Region region) {
                sendDelayedLeave(region);
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {

                Beacon b = getBeaconFromCollection(collection);
                if (b == null) {
                    return;
                }

                processEvent(clientsManager.getBeaconEventFromDistance(b.getDistance()), System.currentTimeMillis(), region);
            }
        });

        beaconManager.setForegroundScanPeriod(config.getForegroundScanDurationInMillis());
        beaconManager.setForegroundBetweenScanPeriod(config.getForegroundPauseDurationInMillis());
        beaconManager.setBackgroundScanPeriod(config.getBackgroundScanDurationInMillis());
        beaconManager.setBackgroundBetweenScanPeriod(config.getBackgroundPauseDurationInMillis());
    }

    private Beacon getBeaconFromCollection(Collection<Beacon> collection) {
        if (collection == null || collection.size() != 1) {
            return null;
        }

        Iterator<Beacon> it = collection.iterator();
        return it.next();
    }

    private class EnterLeaveDelayedHandler extends Handler {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            Pair<Region, Long> obj = (Pair<Region, Long>) msg.obj;
            Region region = obj.first;
            long eventTimestamp = obj.second;

            if (msg.what == BeaconEvent.REGION_ENTER.ordinal()) {
                // if region has been marked to leave, we do not process enter event to have enter/leave events transparent for user
                if (!regionsToLeave.remove(region.getUniqueId()) ) {
                    // region has not been marked to leave, process enter event
                    processEvent(BeaconEvent.REGION_ENTER, eventTimestamp, region);
                }
            } else if (msg.what == BeaconEvent.REGION_LEAVE.ordinal()) {
                if (regionsToLeave.remove(region.getUniqueId())) {
                    processEvent(BeaconEvent.REGION_LEAVE, eventTimestamp, region);
                }
            } else {
                ULog.d(TAG, "EnterLeaveDelayedHandler: unknown message.");
            }
        }
    }

    private String getBeaconProximityId(Region region) {
        if (region == null) return null;

        return region.getId1() + "+" + region.getId2() + "+" + region.getId3();
    }

    private void processEvent(BeaconEvent beaconEvent, long eventTimestamp, Region region) {
        String beaconUniqueId = region.getUniqueId();

        BeaconEvent oldBeaconEvent = clientsManager.getBeaconEvent(beaconUniqueId);
        if (beaconEvent == oldBeaconEvent) {
            ULog.d(TAG, getBeaconProximityId(region) + ", proximity is the same: " + beaconEvent.name());
            return;
        }

        clientsManager.notifyClientsAboutEvent(beaconUniqueId, beaconEvent, eventTimestamp);
        clientsManager.updateBeaconEvent(beaconUniqueId, beaconEvent);
    }

    private void sendDelayedEnter(Region region) {
        Message msg = Message.obtain();
        msg.what = BeaconEvent.REGION_ENTER.ordinal();
        msg.obj = new Pair<>(region, System.currentTimeMillis());
        mEnterLeaveHandler.sendMessage(msg);
    }

    private void sendDelayedLeave(Region region) {
        Message msg = Message.obtain();
        msg.what = BeaconEvent.REGION_LEAVE.ordinal();
        msg.obj = new Pair<>(region, System.currentTimeMillis());
        regionsToLeave.add(region.getUniqueId()); // mark that monitoring of region is intended to be stopped
        mEnterLeaveHandler.sendMessageDelayed(msg, config.getLeaveMsgDelayInMillis());
    }
}