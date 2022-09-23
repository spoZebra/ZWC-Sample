package com.spozebra.zwc_sample.ssm;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;

public class ConfigurationManager {

    static final String TAG = ConfigurationManager.class.getSimpleName();

    private String TARGET_APP_PACKAGE = "target_app_package";
    private String DATA_NAME = "data_name";
    private String DATA_VALUE = "data_value";
    private String DATA_INPUT_FORM = "data_input_form";
    private String DATA_OUTPUT_FORM = "data_output_form";
    private String DATA_PERSIST_REQUIRED = "data_persist_required";
    private String MULTI_INSTANCE_REQUIRED = "multi_instance_required";
    private String paramName = "ZWCConfigured";
    String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";
    Uri cpUri;

    Context context;
    public String APP_PACKAGE_NAME = "";
    private String APP_SIGNATURE = "";

    public ConfigurationManager(Context context) {
        this.context = context;
        this.cpUri = Uri.parse(AUTHORITY);

        APP_PACKAGE_NAME = context.getPackageName();
        APP_SIGNATURE = getPublicSignature(context);
    }

    public String getPublicSignature(Context context) {

        String appSignature = null;
        try {
            Signature signature = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
            if (signature != null) {
                byte[] data = Base64.encode(signature.toByteArray(), Base64.DEFAULT);
                String sign = new String(data, StandardCharsets.UTF_8);
                appSignature = sign.replaceAll("\\s+", "");
                // Util.log(TAG, LogType.INFO, "getPublicSignature: " + appSignature);
            } else {
                Log.e(TAG, "getPublicSignature error");
            }
        } catch (Exception e) {
            Log.e(TAG, "getPublicSignature error" + e.getMessage());
        }
        return appSignature;

    }

    private ContentValues buildContentValues(String paramName, Object value) {
        ContentValues values = new ContentValues();

        values.put(TARGET_APP_PACKAGE,
                "{\"pkgs_sigs\": [{\"pkg\":\"" + APP_PACKAGE_NAME + "\",\"sig\":\"" + APP_SIGNATURE + "\"}]}");

        values.put(DATA_NAME, paramName);
        values.put(DATA_VALUE, String.valueOf(value));
        values.put(DATA_INPUT_FORM, "1"); //plaintext =1, encrypted=2
        values.put(DATA_OUTPUT_FORM, "1"); //plaintext=1, encrypted=2, keystrokes=3
        values.put(DATA_PERSIST_REQUIRED, "false");
        values.put(MULTI_INSTANCE_REQUIRED, "true");

        return values;
    }

    public void updateValue(String paramName, Object value) {
        String selection = TARGET_APP_PACKAGE + "= '" +  context.getPackageName() + "'";
        ContentValues values = new ContentValues();
        int createdRow = context.getContentResolver().update(cpUri, buildContentValues(paramName, value), null, null);
    }


    @SuppressLint("Range")
    public String getValue(String paramName, Object defaultValue) {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + context.getPackageName() + "]");

        String selection = TARGET_APP_PACKAGE + "= '" +  context.getPackageName() + "'" +"AND "+ "data_persist_required = '" + "false" + "'" +
                "AND "+"multi_instance_required = '"+ "true" + "'";

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(cpUriQuery, null, selection, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String name = String.valueOf(cursor.getString(cursor.getColumnIndex(DATA_NAME)));
                    if(name.equals(paramName)){
                        return cursor.getString(cursor.getColumnIndex(DATA_VALUE));
                    }
                    cursor.moveToNext();
                }
            }
            // Param does not exists, create it
            Uri createdRow = context.getContentResolver().insert(cpUri, buildContentValues(paramName, defaultValue));
        } catch (Exception e) {
            Log.e(TAG, "Query data error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return String.valueOf(defaultValue);
    }
}
