/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.upnext.beaconcontrol.sdk.backend.Validable;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeaconProximity implements Serializable, Validable {

    private static final String TAG = BeaconProximity.class.getSimpleName();

    /**
     * Matches beacon proximity id and extracts UUID, Major and Minor from it.
     *
     * That regexp matches:
     * letters_sequence '+' digits_sequence '+' digits_sequence
     *
     * Example:
     * Proximity id: F7826DA6-4FA2-4E98-8024-BC5B71E0893E+53238+25345
     *
     * UUID = matcher.group(PROXIMITY_UUID_GROUP_NO) = F7826DA6-4FA2-4E98-8024-BC5B71E0893E
     * Major = matcher.group(PROXIMITY_MAJOR_GROUP_NO) = 53238
     * Minor = matcher.group(PROXIMITY_MINOR_GROUP_NO) = 25345
     */
    private static final String PROXIMITY_ID_REGEXP = "(.*)\\+(\\d+)\\+(\\d+)";
    private static final Pattern PROXIMITY_ID_PATTERN = Pattern.compile(PROXIMITY_ID_REGEXP);

    private static final int UUID_LENGTH = 36; // 32 + 4 dashes

    private static final int PROXIMITY_UUID_GROUP_NO = 1;
    private static final int PROXIMITY_MAJOR_GROUP_NO = 2;
    private static final int PROXIMITY_MINOR_GROUP_NO = 3;

    private static final int MAJOR_MINOR_MIN_VALUE = 1;
    private static final int MAJOR_MINOR_MAX_VALUE = 65535;

    private String proximityId;

    @JsonCreator
    public BeaconProximity(String proximityId) {
        this.proximityId = proximityId;
    }

    public String getProximityId() {
        return proximityId;
    }

    public String getUUID() {
        Matcher matcher = PROXIMITY_ID_PATTERN.matcher(proximityId);
        if (proximityId == null || !matcher.matches()) {
            return null;
        }

        return matcher.group(PROXIMITY_UUID_GROUP_NO);
    }

    public Integer getMajor() {
        return getProximityNumber(PROXIMITY_MAJOR_GROUP_NO);
    }

    public Integer getMinor() {
        return getProximityNumber(PROXIMITY_MINOR_GROUP_NO);
    }

    private Integer getProximityNumber(int pos) {
        Matcher matcher = PROXIMITY_ID_PATTERN.matcher(proximityId);
        if (proximityId == null || !matcher.matches()) {
            return null;
        }

        try {
            return Integer.parseInt(matcher.group(pos));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean isValid() {
        String uuid = getUUID();
        Integer major = getMajor();
        Integer minor = getMinor();

        return  uuid != null && uuid.length() == UUID_LENGTH &&
                major != null && major >= MAJOR_MINOR_MIN_VALUE && major <= MAJOR_MINOR_MAX_VALUE &&
                minor != null && minor >= MAJOR_MINOR_MIN_VALUE && minor <= MAJOR_MINOR_MAX_VALUE;
    }
}
