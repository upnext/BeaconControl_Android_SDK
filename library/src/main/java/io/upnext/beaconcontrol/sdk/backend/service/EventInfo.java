/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.service;

import android.support.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EventInfo implements Serializable {

    public interface EventSource {
        int BEACON = 0;
        int ZONE = 1;
    }

    @IntDef({EventSource.BEACON, EventSource.ZONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IEventSource {}

    private BeaconEvent beaconEvent;
    private long timestamp;
    private @IEventSource int eventSource;

    public EventInfo(BeaconEvent beaconEvent, long timestamp, @IEventSource int eventSource) {
        this.beaconEvent = beaconEvent;
        this.timestamp = timestamp;
        this.eventSource = eventSource;
    }

    public BeaconEvent getBeaconEvent() {
        return beaconEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @IEventSource int getEventSource() {
        return eventSource;
    }
}
