/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.events;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import java.util.LinkedList;
import java.util.List;

import io.upnext.beaconcontrol.sdk.Action;
import io.upnext.beaconcontrol.sdk.Beacon;
import io.upnext.beaconcontrol.sdk.BeaconControl;
import io.upnext.beaconcontrol.sdk.BeaconControl.ActionReceiver;
import io.upnext.beaconcontrol.sdk.BeaconControl.BeaconProximityChangeReceiver;
import io.upnext.beaconcontrol.sdk.BeaconControl.ConfigurationLoadReceiver;
import io.upnext.beaconcontrol.sdk.ErrorCode;
import io.upnext.beaconcontrol.sdk.backend.BeaconControlManager;
import io.upnext.beaconcontrol.sdk.backend.HttpListener;
import io.upnext.beaconcontrol.sdk.backend.mediator.CreateEventsCallMediator;
import io.upnext.beaconcontrol.sdk.backend.model.CreateEventsRequest;
import io.upnext.beaconcontrol.sdk.backend.model.CreateEventsRequest.Event;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse.Trigger;
import io.upnext.beaconcontrol.sdk.backend.service.BeaconEvent;
import io.upnext.beaconcontrol.sdk.backend.service.BeaconModel;
import io.upnext.beaconcontrol.sdk.backend.service.EventInfo;
import io.upnext.beaconcontrol.sdk.backend.service.EventInfo.EventSource;
import io.upnext.beaconcontrol.sdk.backend.service.EventInfo.IEventSource;
import io.upnext.beaconcontrol.sdk.core.BeaconPreferences;
import io.upnext.beaconcontrol.sdk.core.Config;
import io.upnext.beaconcontrol.sdk.internal.model.BeaconsList;
import io.upnext.beaconcontrol.sdk.util.ULog;

public class EventsManager {

    private static final String TAG = EventsManager.class.getSimpleName();

    private static EventsManager sInstance;
    private static final Object sShouldPerformActionAutomaticallyLock = new Object();
    private static boolean sShouldPerformActionAutomatically = true;

    private final Context context;
    private final Config config;
    private final BeaconPreferences preferences;
    private final BeaconControlManager beaconControlManager;

    public static void setShouldPerformActionAutomatically(boolean shouldPerformActionAutomatically) {
        synchronized (sShouldPerformActionAutomaticallyLock) {
            sShouldPerformActionAutomatically = shouldPerformActionAutomatically;
        }
    }

    private static boolean shouldPerformActionAutomatically() {
        synchronized (sShouldPerformActionAutomaticallyLock) {
            return sShouldPerformActionAutomatically;
        }
    }

    private EventsManager(Context context, Config config, BeaconPreferences preferences, BeaconControlManager beaconControlManager) {
        this.context = context;
        this.config = config;
        this.preferences = preferences;
        this.beaconControlManager = beaconControlManager;
    }

    public static EventsManager getInstance(Context context, Config config, BeaconPreferences preferences, BeaconControlManager beaconControlManager) {
        if (sInstance == null) {
            sInstance = new EventsManager(context, config, preferences, beaconControlManager);
        }
        return sInstance;
    }

    public void processEvent(BeaconModel bm, Trigger trigger, EventInfo eventInfo) {
        if (bm == null || trigger == null || eventInfo == null || eventInfo.getBeaconEvent() == null) {
            ULog.d(TAG, "Cannot process event.");
            return;
        }

        BeaconEvent beaconEvent = eventInfo.getBeaconEvent();
        @IEventSource int eventSource = eventInfo.getEventSource();

        ULog.d(TAG, "beacon: " + bm.getProximityId() + ", event: " + beaconEvent.name() + ", type: "
                + (eventSource == EventSource.BEACON ? "beacon" : "zone") + ".");

        if (beaconEvent == BeaconEvent.REGION_ENTER || beaconEvent == BeaconEvent.REGION_LEAVE) {
            processEnterLeaveEvent(bm, trigger, eventInfo);
        }

        processAction(trigger.action);
    }

    public void processConfigurationLoaded(BeaconsList beaconsList) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ConfigurationLoadReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(ConfigurationLoadReceiver.BEACONS_LIST, beaconsList);
        context.sendBroadcast(broadcastIntent);
    }

    public void processBeaconProximityChanged(Beacon beacon) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BeaconProximityChangeReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(BeaconProximityChangeReceiver.BEACON, beacon);
        context.sendBroadcast(broadcastIntent);
    }

    private void processEnterLeaveEvent(BeaconModel bm, Trigger trigger, EventInfo eventInfo) {
        Event event = getEventFromBeaconEvent(bm, trigger, eventInfo);

        List<Event> events = preferences.getEventsList();
        events.add(event);

        long currentTimeMillis = System.currentTimeMillis();
        if (preferences.getEventsSentTimestamp() + config.getEventsSendoutDelayInMillis() < currentTimeMillis) {
            sendEvents(events);
            preferences.setEventsSentTimestamp(currentTimeMillis);
            preferences.setEventsList(new LinkedList<Event>());
        } else {
            preferences.setEventsList(events);
        }
    }

    private Event getEventFromBeaconEvent(BeaconModel bm, Trigger trigger, EventInfo eventInfo) {
        Event event = new Event();

        event.setEventType(eventInfo.getBeaconEvent() == BeaconEvent.REGION_ENTER ? Event.EventType.enter : Event.EventType.leave);

        if (eventInfo.getEventSource() == EventSource.BEACON) {
            event.setProximityId(bm.getProximityId());
        } else {
            event.setZoneId(bm.getZone().id);
        }

        event.setActionId(trigger.action.id);

        event.setTimestamp(eventInfo.getTimestamp() / DateUtils.SECOND_IN_MILLIS); // timestamp in seconds is sent

        return event;
    }

    private void sendEvents(List<Event> events) {
        new CreateEventsCallMediator(context, beaconControlManager, new HttpListener<Void>() {
            @Override
            public void onSuccess(Void response) {

            }

            @Override
            public void onError(ErrorCode errorCode, Throwable t) {
                ULog.e(TAG, "Error in createEvents task, " + errorCode.name(), t);

                BeaconControl.onError(errorCode);
            }

            @Override
            public void onEnd() {

            }
        }).createEvents(getCreateEventsRequest(events));
    }

    private CreateEventsRequest getCreateEventsRequest(List<Event> events) {
        return new CreateEventsRequest(events);
    }

    private Intent getIntentForActionReceiver() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ActionReceiver.PROCESS_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);

        return broadcastIntent;
    }

    private void onActionStart(Action action) {
        ULog.d(TAG, "onActionStart.");

        Intent broadcastIntent = getIntentForActionReceiver();
        broadcastIntent.putExtra(ActionReceiver.ACTION_START, action);
        context.sendBroadcast(broadcastIntent);
    }

    private void onActionEnd(Action action) {
        ULog.d(TAG, "onActionEnd.");

        Intent broadcastIntent = getIntentForActionReceiver();
        broadcastIntent.putExtra(ActionReceiver.ACTION_END, action);
        context.sendBroadcast(broadcastIntent);
    }

    private void processAction(Action action) {
        if (action == null || action.type == null) {
            ULog.d(TAG, "Cannot process action.");
            return;
        }

        onActionStart(action);

        if (shouldPerformActionAutomatically()) {
            switch (action.type) {
                case url:
                case coupon:
                    if (action.payload != null && action.payload.url != null) {
                        displayPage(action.name, action.payload.url);
                    }
            }
        } else {
            ULog.d(TAG, "Should not perform action automatically.");
        }

        onActionEnd(action);
    }

    private void displayPage(String name, String url) {
        launchActivity(WebViewActivity.getIntent(context, name, url));
    }

    private void launchActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
