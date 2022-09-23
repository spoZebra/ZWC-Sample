package com.spozebra.zwc_sample;

import static android.content.Intent.ACTION_DOCK_EVENT;
import static android.content.Intent.EXTRA_DOCK_STATE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.spozebra.zwc_sample.emdk.EmdkEngine;
import com.spozebra.zwc_sample.emdk.listeners.IEmdkEngineListener;
import com.spozebra.zwc_sample.ssm.ConfigurationManager;
import com.spozebra.zwc_sample.viewmodel.MainActivityViewModel;
import com.symbol.emdk.EMDKResults;

public class MainActivity extends AppCompatActivity implements IEmdkEngineListener {

    ConfigurationManager configurationManager;
    private ImageView zwcCheckIcon;
    private ImageView configCheckIcon;
    private Button zwcInstallButton;
    private Button zwcConfigureButton;
    private ProgressBar progressBar;

    private MainActivityViewModel viewModel;
    private EmdkEngine emdkEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zwcCheckIcon = (ImageView)findViewById(R.id.imageViewCheckZWC);
        configCheckIcon = (ImageView)findViewById(R.id.imageViewCheckConfig);
        zwcInstallButton = (Button)findViewById(R.id.zwcInstallButton);
        zwcConfigureButton = (Button)findViewById(R.id.zwcConfigureButton);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        initUI();

        IntentFilter dockFilter = new IntentFilter(EXTRA_DOCK_STATE);
        dockFilter.addAction(ACTION_DOCK_EVENT);
    }

    private void initUI(){
        configurationManager = new ConfigurationManager(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        progressBar.setVisibility(View.VISIBLE);
        if(viewModel.isZWCInstalled(getApplicationContext(), false)){
            zwcCheckIcon.setImageResource(R.drawable.checked);
            zwcInstallButton.setEnabled(false);
        }
        if(viewModel.isZWCConfigured(configurationManager)) {
            configCheckIcon.setImageResource(R.drawable.checked);
            zwcConfigureButton.setEnabled(false);
        }

        // Init Emdk
        emdkEngine = EmdkEngine.getInstance(getApplicationContext(), this);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        // Force reload as it might be installed while activity was not in foreground
        if(viewModel.isZWCInstalled(getApplicationContext(), true)){
            zwcCheckIcon.setImageResource(R.drawable.checked);
            zwcInstallButton.setEnabled(false);
        }
    }

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
                        viewModel.setZWCConfigured(configurationManager);
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

    @Override
    public void emdkInitialized() {
        progressBar.setVisibility(View.GONE);
    }
}