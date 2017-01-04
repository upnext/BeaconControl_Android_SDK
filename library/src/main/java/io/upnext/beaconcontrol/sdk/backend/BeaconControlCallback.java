/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package io.upnext.beaconcontrol.sdk.backend;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BeaconControlCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            onError(response, null);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onError(null, t);
    }

    protected abstract void onSuccess(T response);

    protected abstract void onError(Response<T> response, Throwable t);
}

