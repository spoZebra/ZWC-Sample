package com.spozebra.zwc_sample.receiver;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.spozebra.zwc_sample.SecondaryDisplayActivity;
import com.spozebra.zwc_sample.listeners.IDockStateListener;

public class DockStateReceiver extends BroadcastReceiver {

    static DockStateReceiver instance;
    private IDockStateListener listener;

    private DockStateReceiver(IDockStateListener listener){
        this.listener = listener;
    }

    public static DockStateReceiver getInstance(IDockStateListener listener) {
        if (instance == null) {
            instance = new DockStateReceiver(listener);
        }
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED);
        if (dockState == 1) {
            listener.cradleConnected();
        } else if (dockState == 0) {
            listener.cradleDisonnected();
        }
    }
}