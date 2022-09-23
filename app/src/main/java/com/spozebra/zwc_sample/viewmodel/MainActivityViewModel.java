package com.spozebra.zwc_sample.viewmodel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.spozebra.zwc_sample.ssm.ConfigurationManager;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Boolean> isConfigured;
    private MutableLiveData<Boolean> isZWCInstalled;

    public MainActivityViewModel(){
    }

    public Boolean isZWCConfigured(ConfigurationManager configurationManager) {
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

    public void setZWCConfigured(ConfigurationManager configurationManager){
        configurationManager.updateValue("isZWCConfigured", true);
        isConfigured.setValue(true);
    }

    public Boolean isZWCInstalled(Context applicationContext, boolean forceReload) {
        if(isZWCInstalled == null || forceReload) {
            Boolean result = false;
            PackageManager pm = applicationContext.getPackageManager();
            try {
                pm.getPackageInfo("com.zebra.workstationconnect.release", PackageManager.GET_ACTIVITIES);
                result = true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("ZWC", e.toString());
            }
            isZWCInstalled = new MutableLiveData<Boolean>();
            isZWCInstalled.setValue(result);
        }

        return isZWCInstalled.getValue();
    }

    /*public EmdkEngine getEmdkEngine(){
        if(emdkEngine == null){
            emdkEngine = new EmdkEngine();
        }
    }*/
}