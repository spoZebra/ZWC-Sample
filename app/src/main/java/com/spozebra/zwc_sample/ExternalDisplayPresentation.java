package com.spozebra.zwc_sample;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

public class ExternalDisplayPresentation extends Presentation {

    public ExternalDisplayPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_display);
    }
}
