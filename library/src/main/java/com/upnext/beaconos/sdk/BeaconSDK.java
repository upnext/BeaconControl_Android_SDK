/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.upnext.beaconos.sdk.backend.BeaconControlManagerImpl;
import com.upnext.beaconos.sdk.backend.BeaconControlManager;
import com.upnext.beaconos.sdk.backend.events.EventsManager;
import com.upnext.beaconos.sdk.backend.model.TokenCredentials;
import com.upnext.beaconos.sdk.backend.service.BeaconServiceHelper;
import com.upnext.beaconos.sdk.core.BeaconPreferences;
import com.upnext.beaconos.sdk.core.BeaconPreferencesImpl;
import com.upnext.beaconos.sdk.core.Config;
import com.upnext.beaconos.sdk.core.ConfigImpl;
import com.upnext.beaconos.sdk.util.ULog;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

/**
 * This class represents singleton object that allows for interaction with Beacon SDK.
 */
public final class BeaconSDK {

    private static final String TAG = BeaconSDK.class.getSimpleName();

    private static final Object sInstanceLock = new Object();
    private static BeaconSDK sInstance;

    private Context context;
    private BeaconServiceHelper beaconServiceHelper;
    private BeaconErrorListener beaconErrorListener;
    private BeaconDelegate beaconDelegate;
    private ActionReceiver actionReceiver;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean started;

    private BeaconSDK(Context context, Config config, BeaconPreferences preferences, BeaconControlManager beaconControlManager, TokenCredentials tokenCredentials) {
        this.context = context;

        preferences.setOAuthCredentials(tokenCredentials);

        beaconServiceHelper = BeaconServiceHelper.getInstance(context, config, beaconControlManager);
        backgroundPowerSaver = new BackgroundPowerSaver(context);
    }

    /**
     * Gets BeaconSDK instance.
     *
     * @param context The Context from which application context is retrieved.
     * @param clientId OAuth client id.
     * @param clientSecret OAuth client secret.
     * @param userId Unique id per application and user.
     * @return Instance of BeaconsSDK.
     */
    public static BeaconSDK getInstance(Context context, String clientId, String clientSecret, String userId) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = getBeaconSDKInstance(context, new TokenCredentials(clientId, clientSecret, userId));
            }
            return sInstance;
        }
    }

    private static BeaconSDK getBeaconSDKInstance(Context context, TokenCredentials tokenCredentials) {
        Context appContext = context.getApplicationContext();
        Config config = ConfigImpl.getInstance(appContext);
        BeaconPreferences preferences = BeaconPreferencesImpl.getInstance(appContext);
        BeaconControlManager beaconControlManager = BeaconControlManagerImpl.getInstance(appContext, config, preferences);

        return new BeaconSDK(appContext, config, preferences, beaconControlManager, tokenCredentials);
    }

    public static void onError(ErrorCode errorCode) {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                sInstance.notifyAboutError(errorCode);
            } else {
                ULog.d(TAG, "onError, instance is null.");
            }
        }
    }

    /**
     * Sets {@link BeaconDelegate} object.
     *
     * @param beaconDelegate Object that implements {@link BeaconDelegate} interface.
     */
    public void setBeaconDelegate(BeaconDelegate beaconDelegate) {
        this.beaconDelegate = beaconDelegate;
        EventsManager.setShouldPerformActionAutomatically(beaconDelegate.shouldPerformActionAutomatically());
    }

    /**
     * Sets listener that informs about errors.
     *
     * @param beaconErrorListener Object that implements {@link BeaconErrorListener} interface.
     */
    public void setBeaconErrorListener(BeaconErrorListener beaconErrorListener) {
        this.beaconErrorListener = beaconErrorListener;
    }

    /**
     * Enable or disable logging.
     *
     * @param enable True if the logging should be enabled, false otherwise.
     */
    public void enableLogging(boolean enable) {
        ULog.enableLogging(enable);
    }

    private void registerActionReceiver() {
        IntentFilter filter = new IntentFilter(ActionReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        actionReceiver = new ActionReceiver();
        context.registerReceiver(actionReceiver, filter);
    }

    private void unregisterActionReceiver() {
        context.unregisterReceiver(actionReceiver);
    }

    /**
     * Starts beacons monitoring.
     */
    public void startScan() {
        if (isBLEAvailable() && isBluetoothEnabled()) {
            if (!started) {
                registerActionReceiver();
            }
            beaconServiceHelper.startScan();
            started = true;
        }
    }

    private void notifyAboutError(ErrorCode errorCode) {
        if (beaconErrorListener != null) {
            beaconErrorListener.onError(errorCode);
        }
    }

    private boolean isBLEAvailable() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            notifyAboutError(ErrorCode.BLE_NOT_SUPPORTED);
            return false;
        }

        return true;
    }

    private boolean isBluetoothEnabled() {
        if ( !((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()) {
            notifyAboutError(ErrorCode.BLUETOOTH_NOT_ENABLED);
            return false;
        }

        return true;
    }

    /**
     * Stops beacons monitoring.
     */
    public void stopScan() {
        if (started) {
            beaconServiceHelper.stopScan();
            unregisterActionReceiver();
            started = false;
        }
    }

    public class ActionReceiver extends BroadcastReceiver {

        private static final String TAG = "BeaconSDK.ActionReceiver"; // added explicitly, because inner classes cannot have a static initializer block

        public static final String PROCESS_RESPONSE = "com.upnext.beaconos.sdk.BeaconSDK.ActionReceiver.PROCESS_RESPONSE";

        public static final String ACTION_START = "com.upnext.beaconos.sdk.BeaconSDK.ActionReceiver.ACTION_START";
        public static final String ACTION_END = "com.upnext.beaconos.sdk.BeaconSDK.ActionReceiver.ACTION_END";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (beaconDelegate == null) {
                ULog.d(TAG, "beaconDelegate is null.");
                return;
            }

            if (intent.hasExtra(ACTION_START)) {
                ULog.d(TAG, "onActionStart.");
                beaconDelegate.onActionStart((Action) intent.getSerializableExtra(ACTION_START));
            } else if (intent.hasExtra(ACTION_END)) {
                ULog.d(TAG, "onActionEnd.");
                beaconDelegate.onActionEnd((Action) intent.getSerializableExtra(ACTION_END));
            } else {
                ULog.d(TAG, "Unknown operation.");
            }
        }
    }
}
