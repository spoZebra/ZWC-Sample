package com.spozebra.zwc_sample;

import android.app.Presentation;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

public class MyPresentationClass extends Presentation {

    public MyPresentationClass(Context outerContext, Display display) {
        super(outerContext, display);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);
        Resources r = getContext().getResources();

        // Inflate the layout.
        setContentView(R.layout.presentation_mode);
    }

    public void setPreferredDisplayMode(int modeId) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.preferredDisplayModeId = modeId;
        getWindow().setAttributes(params);
    }
}
