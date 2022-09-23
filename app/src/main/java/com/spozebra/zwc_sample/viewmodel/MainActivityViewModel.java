package com.spozebra.zwc_sample.viewmodel;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.spozebra.zwc_sample.receiver.DockStateReceiver;
import com.spozebra.zwc_sample.ssm.ConfigurationManager;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Boolean> isConfigured;
    private MutableLiveData<Boolean> isInstalled;
    private MutableLiveData<DockStateReceiver> dockStateReceiver;

    public MainActivityViewModel(){
    }

    public Boolean getIsZwcConfigured(ConfigurationManager configurationManager) {
        if (isConfigured == null) {
            String val = configurationManager.getValue("isZWCConfigured", false);
            Boolean value = false;
            if(val != null)
                value = Boolean.valueOf(val);

            isConfigured = new MutableLiveData<Boolean>();
            isConfigured.setValue(value);
        }
        return isConfigured.getValue();
    }

    public void setIsZwcConfigured(ConfigurationManager configurationManager){
        configurationManager.updateValue("isZWCConfigured", true);
        isConfigured.setValue(true);
    }

    public Boolean getIsZwcInstalled(PackageManager packageManager, boolean forceReload) {
        if(isInstalled == null || forceReload) {
            Boolean result = false;
            try {
                packageManager.getPackageInfo("com.zebra.workstationconnect.release", PackageManager.GET_ACTIVITIES);
                result = true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("ZWC", e.toString());
            }
            isInstalled = new MutableLiveData<Boolean>();
            isInstalled.setValue(result);
        }

        return isInstalled.getValue();
    }

    public DockStateReceiver getDockStateReceiver() {
        if(dockStateReceiver == null){
            dockStateReceiver = new MutableLiveData<DockStateReceiver>();
            dockStateReceiver.setValue(new DockStateReceiver());
        }

        return dockStateReceiver.getValue();
    }
}