/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.mediator;

import android.content.Context;

import com.upnext.beaconos.sdk.backend.HttpListener;
import com.upnext.beaconos.sdk.backend.BeaconControlManager;
import com.upnext.beaconos.sdk.backend.model.CreateEventsRequest;
import com.upnext.beaconos.sdk.backend.model.Empty;

public class CreateEventsCallMediator extends HttpCallMediator<Empty> {

    private CreateEventsRequest request;

    public CreateEventsCallMediator(Context context, BeaconControlManager beaconControlManager, HttpListener<Empty> httpListener) {
        super(context, beaconControlManager, httpListener);
    }

    public void createEvents(CreateEventsRequest request) {
        this.request = request;

        onStartCall();
    }

    @Override
    protected void execute() {
        setCall(getBeaconControlManager().createEventsCall(request));
    }
}
