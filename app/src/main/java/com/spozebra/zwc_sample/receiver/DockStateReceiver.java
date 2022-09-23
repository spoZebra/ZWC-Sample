package com.spozebra.zwc_sample.receiver;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.spozebra.zwc_sample.SecondaryDisplayActivity;

public class DockStateReceiver extends BroadcastReceiver {

    public DockStateReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED);
        if (dockState == 1) {

            DisplayManager dm = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = dm.getDisplays();
            for (Display display : displays) {
                String name = display.getName();
                if (name.startsWith("DisplayLink") || name.startsWith("HDMI Screen")) {
                    ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

                    Intent startActivityIntent = new Intent(context, SecondaryDisplayActivity.class);
                    startActivityIntent.addFlags(startActivityIntent.FLAG_ACTIVITY_MULTIPLE_TASK|startActivityIntent.FLAG_ACTIVITY_NEW_TASK);

                    boolean activityAllowed = activityManager.isActivityStartAllowedOnDisplay(context, display.getDisplayId(), startActivityIntent);
                    if(activityAllowed){
                        ActivityOptions options = ActivityOptions.makeBasic();
                        options.setLaunchDisplayId(display.getDisplayId());
                        context.startActivity(startActivityIntent, options.toBundle());
                    }
                }
            }
        } else if (dockState == 0) {
            // CLose
        }
    }
}