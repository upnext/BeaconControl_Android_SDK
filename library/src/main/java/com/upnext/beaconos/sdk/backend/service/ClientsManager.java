/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse.Range;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse.Zone;
import com.upnext.beaconos.sdk.backend.model.GetConfigurationsResponse.Trigger;
import com.upnext.beaconos.sdk.backend.service.EventInfo.EventSource;
import com.upnext.beaconos.sdk.util.ULog;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientsManager {

    private static final String TAG = ClientsManager.class.getSimpleName();

    private Context context;
    private BeaconManager beaconManager;
    private Set<String> clients = new HashSet<>();
    private Map<BeaconModel, BeaconEvent> monitoredBeacons = new HashMap<>();

    protected ClientsManager(Context context, BeaconManager beaconManager) {
        this.context = context;
        this.beaconManager = beaconManager;
    }

    protected boolean hasClients() {
        return !clients.isEmpty();
    }

    protected boolean containsClient(String clientId) {
        return clients.contains(clientId);
    }

    protected void addClient(String clientId, GetConfigurationsResponse conf) {
        clients.add(clientId);

        addMonitoredBeacons(clientId, conf);
    }

    protected void updateClient(String clientId, GetConfigurationsResponse conf) {
        clients.add(clientId);

        removeMonitoredBeacons(clientId);
        addMonitoredBeacons(clientId, conf);
    }

    protected void removeClient(String clientId) {
        removeMonitoredBeacons(clientId);

        clients.remove(clientId);
    }

    protected BeaconEvent getBeaconEvent(String beaconUniqueId) {
        BeaconModel bm = getMonitoredBeaconModel(beaconUniqueId);
        return bm == null ? null : monitoredBeacons.get(bm);
    }

    protected void updateBeaconEvent(String beaconUniqueId, BeaconEvent event) {
        BeaconModel bm = getMonitoredBeaconModel(beaconUniqueId);
        if (bm != null) {
            monitoredBeacons.put(bm, event);
        }
    }

    protected void notifyClientsAboutEvent(String beaconUniqueId, BeaconEvent beaconEvent, long eventTimestamp) {
        BeaconModel bm = getMonitoredBeaconModel(beaconUniqueId);
        if (bm == null) return;

        EventInfo eventInfo = new EventInfo(beaconEvent, eventTimestamp, EventSource.BEACON);
        notifyClientsAboutEvent(bm, bm.getBeaconTriggersForEvent(beaconEvent), eventInfo);

        if (bm.getZone() != null) {
            eventInfo = new EventInfo(beaconEvent, eventTimestamp, EventSource.ZONE);
            notifyClientsAboutEvent(bm, bm.getZoneTriggersForEvent(beaconEvent), eventInfo);
        }
    }

    protected BeaconEvent getBeaconEventFromDistance(double distance) {
        return BeaconEvent.fromBeaconDistance(distance);
    }

    private BeaconModel getMonitoredBeaconModel(String beaconUniqueId) {
        for (BeaconModel bm : monitoredBeacons.keySet()) {
            if (bm.getUniqueId().equals(beaconUniqueId)) {
                return bm;
            }
        }
        return null;
    }

    private void addMonitoredBeacons(String clientId, GetConfigurationsResponse conf) {
        for (Range range : conf.ranges) {
            Map.Entry<BeaconModel, BeaconEvent> beaconEntry = getMonitoredBeaconEntry(range.id);

            BeaconModel bm;
            BeaconEvent event;
            if (beaconEntry == null || beaconEntry.getKey() == null) {
                bm = new BeaconModel(range);
                Zone zone = getBeaconZone(bm.getId(), conf.zones);
                if (zone != null) {
                    bm.setZone(zone);
                }

                event = BeaconEvent.UNKNOWN;

                addNewRegion(bm);
            } else {
                bm = beaconEntry.getKey();
                event = beaconEntry.getValue();
            }

            addTriggers(clientId, bm, conf.triggers);

            bm.addClient(clientId);

            monitoredBeacons.put(bm, event);
        }
    }

    private void addTriggers(String clientId, BeaconModel bm, List<Trigger> triggers) {
        for (Trigger trigger : triggers) {
            if (trigger.range_ids != null && trigger.range_ids.contains(bm.getId())) {
                bm.addBeaconTrigger(clientId, trigger);
            }
            if (trigger.zone_ids != null && bm.getZone() != null && trigger.zone_ids.contains(bm.getZone().id)) {
                bm.addZoneTrigger(clientId, trigger);
            }
        }
    }

    private void addNewRegion(BeaconModel bm) {
        Region region = new Region(bm.getUniqueId(), Identifier.parse(bm.getProximityUUID()), Identifier.parse(bm.getProximityMajor().toString()), Identifier.parse(bm.getProximityMinor().toString()));
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            ULog.d(TAG, "Cannot start monitor/range region.");
        }
    }

    private Map.Entry<BeaconModel, BeaconEvent> getMonitoredBeaconEntry(long beaconId) {
        for (Map.Entry<BeaconModel, BeaconEvent> entry : monitoredBeacons.entrySet()) {
            if (entry.getKey().getId() == beaconId) {
                return entry;
            }
        }
        return null;
    }

    private Zone getBeaconZone(long beaconId, List<Zone> zones) {
        for (Zone zone : zones) {
            if (zone.beaconIds.contains(beaconId)) {
                return zone;
            }
        }
        return null;
    }

    private void removeMonitoredBeacons(String clientId) {
        Map<BeaconModel, BeaconEvent> newMonitoredBeacons = new HashMap<>();

        for (Map.Entry<BeaconModel, BeaconEvent> entry : monitoredBeacons.entrySet()) {
            BeaconModel bm = entry.getKey();
            if (bm.containsClient(clientId)) {
                bm.removeClient(clientId);
                removeTriggers(clientId, bm);
            }
            if (bm.hasClients()) {
                newMonitoredBeacons.put(bm, entry.getValue());
            } else {
                try {
                    beaconManager.stopMonitoringBeaconsInRegion(new Region(bm.getUniqueId(), null, null, null));
                    beaconManager.stopRangingBeaconsInRegion(new Region(bm.getUniqueId(), null, null, null));
                } catch (RemoteException e) {
                    ULog.d(TAG, "Cannot stop monitor/range region.");
                }
            }
        }
        monitoredBeacons = newMonitoredBeacons;
    }

    private void removeTriggers(String clientId, BeaconModel bm) {
        bm.removeClientTriggers(clientId);
    }

    private void notifyClientsAboutEvent(BeaconModel bm, Map<String, List<Trigger>> eventTriggers, EventInfo eventInfo) {
        for (Map.Entry<String, List<Trigger>> entry : eventTriggers.entrySet()) {
            String client = entry.getKey();
            for (Trigger trigger : entry.getValue()) {
                Intent i = new Intent();
                i.setComponent(new ComponentName(client, BeaconEventProcessor.class.getCanonicalName()));

                i.putExtra(BeaconEventProcessor.Extra.BEACON, bm);
                i.putExtra(BeaconEventProcessor.Extra.TRIGGER, trigger);
                i.putExtra(BeaconEventProcessor.Extra.EVENT, eventInfo);

                context.startService(i);
            }
        }
    }
}
