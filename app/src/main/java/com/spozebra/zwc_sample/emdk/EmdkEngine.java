package com.spozebra.zwc_sample.emdk;

import android.content.Context;
import android.util.Log;

import com.spozebra.zwc_sample.emdk.listeners.IEmdkEngineListener;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

public class EmdkEngine implements EMDKManager.EMDKListener {

    private static EmdkEngine instance;
    private static IEmdkEngineListener listener;
    //Declare a variable to store ProfileManager object
    private ProfileManager profileManager = null;

    //Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    private EmdkEngine(Context context, IEmdkEngineListener listener) {

        this.listener = listener;

        //The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(context, this);

        //Check the return status of EMDKManager object creation.
        if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
            //EMDKManager object creation success
        } else {
            //EMDKManager object creation failed
        }
    }

    public EMDKResults setProfile(String profileName, String[] extraData) {
        try {
            EMDKResults result = profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, extraData);
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    public EMDKResults getProfile(String profileName, String[] extraData) {
        EMDKResults result = profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.GET, extraData);
        return result;
    }

    public static EmdkEngine getInstance(Context context, IEmdkEngineListener listener) {
        if (instance == null) {
            instance = new EmdkEngine(context, listener);
        }else{
            listener.emdkInitialized();
        }
        return instance;
    }

    @Override
    public void onClosed() {

        //This callback will be issued when the EMDK closes unexpectedly.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }

        Log.d("Emdk", "EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {

        //This callback will be issued when the EMDK is ready to use.
        Log.d("Emdk", "EMDK open success.");

        this.emdkManager = emdkManager;

        //Get the ProfileManager object to process the profiles
        profileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        listener.emdkInitialized();
    }

    protected void releaseResources() {

        //Clean up the objects created by EMDK manager
        if (profileManager != null)
            profileManager = null;

        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }
}
