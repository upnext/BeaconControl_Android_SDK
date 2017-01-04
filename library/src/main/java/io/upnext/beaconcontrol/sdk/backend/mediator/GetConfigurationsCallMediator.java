/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend.mediator;

import android.content.Context;

import io.upnext.beaconcontrol.sdk.backend.HttpListener;
import io.upnext.beaconcontrol.sdk.backend.BeaconControlManager;
import io.upnext.beaconcontrol.sdk.backend.model.GetConfigurationsResponse;

public class GetConfigurationsCallMediator extends HttpCallMediator<GetConfigurationsResponse> {

    public GetConfigurationsCallMediator(Context context, BeaconControlManager beaconControlManager, HttpListener<GetConfigurationsResponse> httpListener) {
        super(context, beaconControlManager, httpListener);
    }

    public void getConfigurations() {
        onStartCall();
    }

    @Override
    protected void execute() {
        setCall(getBeaconControlManager().getConfigurationsCall());
    }
}
