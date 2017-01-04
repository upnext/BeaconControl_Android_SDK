/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.upnext.beaconcontrol.sdk.Beacon;
import io.upnext.beaconcontrol.sdk.Beacon.Proximity;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse.Range;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse.Trigger;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse.Zone;
import io.upnext.beaconcontrol.sdk.backend.service.EventInfo.EventSource;
import io.upnext.beaconcontrol.sdk.internal.model.BeaconsList;
import io.upnext.beaconcontrol.sdk.util.ULog;

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
        notifyClientsAboutConfigurationLoaded(clientId, conf);
    }

    protected void updateClient(String clientId, GetConfigurationsResponse conf) {
        clients.add(clientId);

        removeMonitoredBeacons(clientId);
        addMonitoredBeacons(clientId, conf);
        notifyClientsAboutConfigurationLoaded(clientId, conf);
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

    protected void notifyClientsAboutAction(String beaconUniqueId, BeaconEvent beaconEvent, long eventTimestamp, double distance) {
        BeaconModel bm = getMonitoredBeaconModel(beaconUniqueId);
        if (bm == null) return;

        EventInfo eventInfo = new EventInfo(beaconEvent, eventTimestamp, EventSource.BEACON);
        notifyClientsAboutBeaconProximityChange(bm, eventInfo, distance);
        notifyClientsAboutAction(bm, bm.getBeaconTriggersForEvent(beaconEvent), eventInfo);

        if (bm.getZone() != null) {
            eventInfo = new EventInfo(beaconEvent, eventTimestamp, EventSource.ZONE);
            notifyClientsAboutAction(bm, bm.getZoneTriggersForEvent(beaconEvent), eventInfo);
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
            ULog.e(TAG, "Cannot start monitor/range region.", e);
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

    private void notifyClientsAboutAction(BeaconModel bm, Map<String, List<Trigger>> eventTriggers, EventInfo eventInfo) {
        for (Map.Entry<String, List<Trigger>> entry : eventTriggers.entrySet()) {
            String client = entry.getKey();
            for (Trigger trigger : entry.getValue()) {
                Bundle extrasBundle = new Bundle();
                extrasBundle.putSerializable(BeaconActionProcessor.Extra.BEACON, bm);
                extrasBundle.putSerializable(BeaconActionProcessor.Extra.TRIGGER, trigger);
                extrasBundle.putSerializable(BeaconActionProcessor.Extra.EVENT, eventInfo);

                notifyClients(client, extrasBundle, BeaconActionProcessor.class);
            }
        }
    }

    private void notifyClients(String clientId, Bundle extras, Class notificationProcessorClass) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(clientId, notificationProcessorClass.getCanonicalName()));
        intent.putExtras(extras);
        context.startService(intent);
    }

    private void notifyClientsAboutBeaconProximityChange(BeaconModel beaconModel, EventInfo eventInfo, double distance) {
        final Beacon beacon = new Beacon(
                beaconModel.getId(),
                beaconModel.getName(),
                beaconModel.getProximityUUID(),
                beaconModel.getProximityMajor(),
                beaconModel.getProximityMinor(),
                mapBeaconEventToProximity(eventInfo.getBeaconEvent()),
                distance
        );
        for (String clientId : beaconModel.getClients()) {
            Bundle extrasBundle = new Bundle();
            extrasBundle.putSerializable(BeaconProximityChangeProcessor.Extra.BEACON, beacon);
            notifyClients(clientId, extrasBundle, BeaconProximityChangeProcessor.class);
        }
    }

    private void notifyClientsAboutConfigurationLoaded(String clientId, GetConfigurationsResponse configurationsResponse) {
        Bundle extrasBundle = new Bundle();
        extrasBundle.putSerializable(BeaconConfigurationChangeProcessor.Extra.BEACONS_LIST, getMonitoredBeaconsListWithCurrentProximity(configurationsResponse));
        notifyClients(clientId, extrasBundle, BeaconConfigurationChangeProcessor.class);
    }

    private BeaconsList getMonitoredBeaconsListWithCurrentProximity(GetConfigurationsResponse configurationsResponse) {
        final List<Beacon> beaconsList = new ArrayList<>();

        for (Range range : configurationsResponse.ranges) {
            Map.Entry<BeaconModel, BeaconEvent> beaconEntry = getMonitoredBeaconEntry(range.id);
            Beacon beacon = new Beacon(
                    range.id,
                    range.name,
                    range.proximityId.getUUID(),
                    range.proximityId.getMajor(),
                    range.proximityId.getMinor(),
                    Proximity.OUT_OF_RANGE,
                    Beacon.DISTANCE_UNDEFINED
            );
            if (beaconEntry != null && beaconEntry.getValue() != null) {
                beacon.currentProximity = mapBeaconEventToProximity(beaconEntry.getValue());
            }
            beaconsList.add(beacon);
        }
        return new BeaconsList(beaconsList);
    }

    private Beacon.Proximity mapBeaconEventToProximity(BeaconEvent event) {
        switch (event) {
            case CAME_IMMEDIATE:
                return Proximity.IMMEDIATE;
            case CAME_NEAR:
                return Proximity.NEAR;
            case CAME_FAR:
            case REGION_ENTER:
                return Proximity.FAR;
            default:
                return Proximity.OUT_OF_RANGE;
        }
    }
}
