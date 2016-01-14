/*
 * Copyright (c) 2016, Upnext Technologies Sp. z o.o.
 * All rights reserved.
 *
 * This source code is licensed under the BSD 3-Clause License found in the
 * LICENSE.txt file in the root directory of this source tree.
 */

package com.upnext.beaconos.sdk.backend.mediator;

import android.content.Context;

import com.upnext.beaconos.sdk.ErrorCode;
import com.upnext.beaconos.sdk.backend.BeaconControlCallback;
import com.upnext.beaconos.sdk.backend.HttpListener;
import com.upnext.beaconos.sdk.backend.BeaconControlManager;
import com.upnext.beaconos.sdk.util.ULog;
import com.upnext.beaconos.sdk.util.NetworkUtils;

import retrofit.Call;
import retrofit.Response;

public abstract class HttpCallMediator<R> {

    private static final String TAG = HttpCallMediator.class.getSimpleName();

    private final Context context;
    private final BeaconControlManager beaconControlManager;
    private final HttpListener<R> listener;

    private Call<R> call;
    private boolean callCancelled;

    protected HttpCallMediator(Context context, BeaconControlManager beaconControlManager, HttpListener<R> listener) {
        this.context = context;
        this.beaconControlManager = beaconControlManager;
        this.listener = listener;
    }

    protected BeaconControlManager getBeaconControlManager() {
        return beaconControlManager;
    }

    protected void setCall(Call<R> call) {
        this.call = call;
    }

    private synchronized void setCallCancelled(boolean callCancelled) {
        this.callCancelled = callCancelled;
    }

    private synchronized boolean getCallCancelled() {
        return callCancelled;
    }

    private boolean callNotCancelledAndListenerPresent() {
        return !getCallCancelled() && listener != null;
    }

    private void notifyOnSuccess(R response) {
        if (callNotCancelledAndListenerPresent()) {
            listener.onSuccess(response);
        }
    }

    private void notifyOnError(ErrorCode errorCode) {
        ULog.d(TAG, errorCode.name());
        if (callNotCancelledAndListenerPresent()) {
            listener.onError(errorCode);
        }
    }

    private void notifyOnEnd() {
        call = null;
        if (callNotCancelledAndListenerPresent()) {
            listener.onEnd();
        }
    }

    protected void onStartCall() {
        if (!NetworkUtils.isOnline(context)) {
            notifyOnError(ErrorCode.OFFLINE);
            return;
        }

        setCallCancelled(false);

        execute();
        enqueueCall(call);
    }

    private void enqueueCall(Call<R> call) {
        call.enqueue(new BeaconControlCallback<R>() {
            @Override
            protected void onSuccess(R response) {
                notifyOnEnd();
                notifyOnSuccess(response);
            }

            @Override
            protected void onError(Response<R> response, Throwable t) {
                notifyOnEnd();
                notifyOnError(getErrorCode(response, t));
            }
        });
    }

    private ErrorCode getErrorCode(Response<R> response, Throwable t) {
        if (response == null) {
            return ErrorCode.IO_ERROR;
        } else {
            return ErrorCode.BEACON_CONTROL_ERROR;
        }
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
            setCallCancelled(true);
        }
    }

    protected abstract void execute();

    public static void cancelHttpMediator(HttpCallMediator mediator) {
        if (mediator != null) {
            mediator.cancel();
        }
    }
}
