package com.spozebra.zwc_sample;

import static android.content.Intent.ACTION_DOCK_EVENT;
import static android.content.Intent.EXTRA_DOCK_STATE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spozebra.zwc_sample.emdk.EmdkEngine;
import com.spozebra.zwc_sample.listeners.IDockStateListener;
import com.spozebra.zwc_sample.listeners.IEmdkEngineListener;
import com.spozebra.zwc_sample.receiver.DockStateReceiver;
import com.spozebra.zwc_sample.ssm.ConfigurationManager;
import com.spozebra.zwc_sample.viewmodel.MainActivityViewModel;
import com.symbol.emdk.EMDKResults;

public class MainActivity extends AppCompatActivity implements IEmdkEngineListener, IDockStateListener {

    private static final String TAG = ConfigurationManager.class.getSimpleName();

    private ImageView zwcCheckIcon;
    private ImageView configCheckIcon;
    private Button zwcInstallButton;
    private Button zwcConfigureButton;
    private ProgressBar progressBar;
    private TextView textViewStatus;

    private MyPresentationClass mPresentation;

    ConfigurationManager configurationManager;
    private MainActivityViewModel viewModel;
    private EmdkEngine emdkEngine;
    private MediaRouter mMediaRouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the media router service.
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);

        zwcCheckIcon = (ImageView)findViewById(R.id.imageViewCheckZWC);
        configCheckIcon = (ImageView)findViewById(R.id.imageViewCheckConfig);
        zwcInstallButton = (Button)findViewById(R.id.zwcInstallButton);
        zwcConfigureButton = (Button)findViewById(R.id.zwcConfigureButton);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        textViewStatus = (TextView)findViewById(R.id.textViewStatus);

        initUI();

    }

    private void initUI(){
        configurationManager = new ConfigurationManager(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        progressBar.setVisibility(View.VISIBLE);

        if(!viewModel.areBroadcastReceiverRegistered()){
            // Add broadcast receiver only once
            IntentFilter dockFilter = new IntentFilter(EXTRA_DOCK_STATE);
            dockFilter.addAction(ACTION_DOCK_EVENT);
            registerReceiver(DockStateReceiver.getInstance(this), dockFilter);
            viewModel.setBroadcastReceiverRegistered();
        }

        checkZwcAppAndConfig(false);

        // Init Emdk (static, initialized once).
        emdkEngine = EmdkEngine.getInstance(getApplicationContext(), this);
    }

    private void checkZwcAppAndConfig(Boolean forceReload) {
        boolean isZwcAppInstalled = viewModel.getIsZwcInstalled(getApplicationContext().getPackageManager(), forceReload);
        if(isZwcAppInstalled){
            zwcCheckIcon.setImageResource(R.drawable.checked);
            zwcInstallButton.setEnabled(false);

            boolean isZwcAppConfigured = viewModel.getIsZwcConfigured(configurationManager);
            if(isZwcAppConfigured) {
                configCheckIcon.setImageResource(R.drawable.checked);
                zwcConfigureButton.setEnabled(false);
                textViewStatus.setText("Connect your device to a cradle");
            }
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        // Force reload as it might be installed while activity was not in foreground
        checkZwcAppAndConfig(true);
    }

    // Buttons
    
    public void onInstallZWCCliecked(View v){
        Intent i = new Intent(android.content.Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.zebra.workstationconnect.release"));
        this.startActivity(i);
    }

    public void onConfigureZWCCliecked(View v){
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Apply MX Config")
                .setMessage("Automatic Reboot will be performed once the profile is applied")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Apply config
                        EMDKResults result = emdkEngine.setProfile("ConfigProfile", null);
                        // Set configured
                        viewModel.setIsZwcConfigured(configurationManager);
                        // Restart
                        emdkEngine.setProfile("RestartProfile", null);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.create();

        builder.show();
    }

    // Dock State check

    @Override
    public void cradleConnected() {
        if(viewModel.getPresentationModeOn())
            LaunchPresentationMode();
        else
            LaunchActivityMode();
    }

    @Override
    public void cradleDisonnected() {
        // Dismiss presentation if exists
        if(mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    // Launch activity
    public  void LaunchActivityMode(){
        DisplayManager dm = (DisplayManager)getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        for (Display display : displays) {
            String name = display.getName();
            if (name.startsWith("DisplayLink") || name.startsWith("HDMI Screen")) {
                ActivityManager activityManager = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

                Intent startActivityIntent = new Intent(getApplicationContext(), SecondaryDisplayActivity.class);
                startActivityIntent.addFlags(startActivityIntent.FLAG_ACTIVITY_LAUNCH_ADJACENT | startActivityIntent.FLAG_ACTIVITY_MULTIPLE_TASK | startActivityIntent.FLAG_ACTIVITY_NEW_TASK);

                boolean activityAllowed = activityManager.isActivityStartAllowedOnDisplay(getApplicationContext(), display.getDisplayId(), startActivityIntent);
                if(activityAllowed){
                    ActivityOptions options = ActivityOptions.makeBasic();
                    options.setLaunchDisplayId(display.getDisplayId());
                    getApplicationContext().startActivity(startActivityIntent, options.toBundle());
                }
            }
        }
    }

    // Launch presentation
    public void LaunchPresentationMode(){
        DisplayManager dm = (DisplayManager)getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = dm.getDisplays();
        Display presentationDisplay = null;
        for (Display display : displays) {
            String name = display.getName();
            if (name.startsWith("DisplayLink") || name.startsWith("HDMI Screen")) {
                presentationDisplay = display;
            }
        }
        /*
        MediaRouter mediaRouter = (MediaRouter) getApplicationContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        if (route != null) {
            presentationDisplay = route.getPresentationDisplay();
            if (presentationDisplay != null) {
                Presentation presentation = new MyPresentationClass(MainActivity.this, presentationDisplay);
                presentation.show();
            }
        }*/
        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            mPresentation = new MyPresentationClass(this, presentationDisplay);
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in the meantime.", ex);
                mPresentation = null;
            }
        }

    }
    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        Log.i(TAG, "Presentation was dismissed.");
                        mPresentation = null;
                    }
                }
            };


    // EMDK

    @Override
    public void emdkInitialized() {
        progressBar.setVisibility(View.GONE);
    }

}