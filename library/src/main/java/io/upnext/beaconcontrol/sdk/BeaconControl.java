/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk;

import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import io.upnext.beaconcontrol.sdk.backend.BeaconControlManager;
import io.upnext.beaconcontrol.sdk.backend.BeaconControlManagerImpl;
import io.upnext.beaconcontrol.sdk.backend.events.EventsManager;
import io.upnext.beaconcontrol.sdk.backend.model.TokenCredentials;
import io.upnext.beaconcontrol.sdk.backend.service.BeaconServiceHelper;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferences;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferencesImpl;
import io.upnext.beaconcontrol.sdk.core.Config;
import io.upnext.beaconcontrol.sdk.core.ConfigImpl;
import io.upnext.beaconcontrol.sdk.internal.model.BeaconsList;
import io.upnext.beaconcontrol.sdk.util.ULog;

/**
 * This class represents singleton object that allows for interaction with BeaconControl SDK.
 */
public final class BeaconControl {

    private static final String TAG = BeaconControl.class.getSimpleName();

    private static final Object sInstanceLock = new Object();
    private static BeaconControl sInstance;

    private final Context context;
    private final BeaconServiceHelper beaconServiceHelper;
    private BeaconErrorListener beaconErrorListener;
    private BeaconDelegate beaconDelegate;
    private BroadcastReceiver actionReceiver;
    private BroadcastReceiver configurationLoadReceiver;
    private BroadcastReceiver beaconProximityChangeReceiver;
    private final BackgroundPowerSaver backgroundPowerSaver;
    private boolean started;

    private BeaconControl(Context context, Config config, BeaconPreferences preferences, BeaconControlManager beaconControlManager, TokenCredentials tokenCredentials) {
        this.context = context;

        preferences.setOAuthCredentials(tokenCredentials);

        beaconServiceHelper = BeaconServiceHelper.getInstance(context, config, beaconControlManager);
        backgroundPowerSaver = new BackgroundPowerSaver(context);
    }

    /**
     * Gets BeaconControl instance.
     *
     * @param context      The Context from which application context is retrieved.
     * @param clientId     OAuth client id.
     * @param clientSecret OAuth client secret.
     * @param userId       Unique id per application and user.
     * @return Instance of BeaconControl.
     */
    public static BeaconControl getInstance(Context context, String clientId, String clientSecret, String userId) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = getBeaconSDKInstance(context, new TokenCredentials(clientId, clientSecret, userId));
            }
            return sInstance;
        }
    }

    private static BeaconControl getBeaconSDKInstance(Context context, TokenCredentials tokenCredentials) {
        Context appContext = context.getApplicationContext();
        Config config = ConfigImpl.getInstance(appContext);
        BeaconPreferences preferences = BeaconPreferencesImpl.getInstance(appContext);
        BeaconControlManager beaconControlManager = BeaconControlManagerImpl.getInstance(appContext, config, preferences);

        return new BeaconControl(appContext, config, preferences, beaconControlManager, tokenCredentials);
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

    private void registerConfigurationLoadReceiver() {
        IntentFilter filter = new IntentFilter(ConfigurationLoadReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        configurationLoadReceiver = new ConfigurationLoadReceiver();
        context.registerReceiver(configurationLoadReceiver, filter);
    }

    private void registerBeaconProximityChangeReceiver() {
        IntentFilter filter = new IntentFilter(BeaconProximityChangeReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        beaconProximityChangeReceiver = new BeaconProximityChangeReceiver();
        context.registerReceiver(beaconProximityChangeReceiver, filter);
    }

    private void unregisterReceivers() {
        context.unregisterReceiver(actionReceiver);
        context.unregisterReceiver(configurationLoadReceiver);
        context.unregisterReceiver(beaconProximityChangeReceiver);
    }

    /**
     * Starts beacons monitoring.
     */
    public void startScan() {
        if (isBLEAvailable() && isBluetoothEnabled()) {
            if (!started) {
                registerReceivers();
            }
            beaconServiceHelper.startScan();
            started = true;
        }
    }

    private void registerReceivers() {
        registerActionReceiver();
        registerConfigurationLoadReceiver();
        registerBeaconProximityChangeReceiver();
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
        if (!((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()) {
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
            unregisterReceivers();
            started = false;
            sInstance = null;
        }
    }

    /**
     * Reloads configuration from the backend.
     * This method should be called only if the scanning is already started (startScan() was called).
     */
    public void reloadConfiguration() {
        if (started) {
            beaconServiceHelper.getConfigurationsAsync();
        } else {
            Log.w(TAG, "Cannot reload configuration as the service is not yet started, call startScan() if not called yet.");
        }
    }

    public class ActionReceiver extends BroadcastReceiver {

        private static final String TAG = "ActionReceiver"; // added explicitly, because inner classes cannot have a static initializer block

        public static final String PROCESS_RESPONSE = "BeaconControl.ActionReceiver.PROCESS_RESPONSE";

        public static final String ACTION_START = "BeaconControl.ActionReceiver.ACTION_START";
        public static final String ACTION_END = "BeaconControl.ActionReceiver.ACTION_END";

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

    public class ConfigurationLoadReceiver extends BroadcastReceiver {

        private static final String TAG = "ConfigurationLoadReceiver";
        public static final String PROCESS_RESPONSE = "BeaconControl.ConfigurationLoadReceiver.PROCESS_RESPONSE";
        public static final String BEACONS_LIST = "BeaconControl.ConfigurationLoadReceiver.BEACONS_LIST";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (beaconDelegate == null) {
                ULog.d(TAG, "beaconDelegate is null.");
                return;
            }

            if (intent.hasExtra(BEACONS_LIST)) {
                BeaconsList beaconsList = (BeaconsList) intent.getSerializableExtra(BEACONS_LIST);
                beaconDelegate.onBeaconsConfigurationLoaded(beaconsList.beaconList);
            } else {
                ULog.d(TAG, "Unknown operation.");
            }
        }
    }

    public class BeaconProximityChangeReceiver extends BroadcastReceiver {

        private static final String TAG = "BeaconProximityChangeReceiver";
        public static final String PROCESS_RESPONSE = "BeaconControl.BeaconProximityChangeReceiver.PROCESS_RESPONSE";
        public static final String BEACON = "BeaconControl.BeaconProximityChangeReceiver.BEACON";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (beaconDelegate == null) {
                ULog.d(TAG, "beaconDelegate is null.");
                return;
            }

            if (intent.hasExtra(BEACON)) {
                Beacon beacon = (Beacon) intent.getSerializableExtra(BEACON);
                beaconDelegate.onBeaconProximityChanged(beacon);
            } else {
                ULog.d(TAG, "Unknown operation.");
            }
        }
    }
}