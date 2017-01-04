/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.model;

import android.graphics.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.upnext.beaconcontrol.sdk.util.ULog;

import java.io.Serializable;

public class BeaconSDKColor implements Serializable {

    private static final String TAG = BeaconSDKColor.class.getSimpleName();

    private static final int DEFAULT_COLOR_VALUE = Color.WHITE;

    private String colorHex;
    private int colorValue;

    @JsonCreator
    public BeaconSDKColor(String colorHex) {
        this.colorHex = colorHex;

        if (colorHex == null) {
            ULog.d(TAG, "Color hex is null.");
            colorValue = DEFAULT_COLOR_VALUE;
        } else {
            try {
                colorValue = Color.parseColor(colorHex.charAt(0) == '#' ? colorHex : "#" + colorHex);
            } catch (IllegalArgumentException e) {
                ULog.d(TAG, "Cannot parse color.");
                colorValue = DEFAULT_COLOR_VALUE;
            }
        }
    }

    public String getColorHex() {
        return colorHex;
    }

    public int getColorValue() {
        return colorValue;
    }
}
