package com.zeltak.app.callnotifier;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Main extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private final String TAG = "Main";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] perms = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
        };

        boolean perm_ok = true;
        for(String p : perms) {
            if(ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                perm_ok = false;
                break;
            }
        }
        if(!perm_ok) {
            Log.i(TAG, "closing main activity");
            try {
                requestPermissions(perms, 1);
                Log.i(TAG, "Permissions granted");
            } catch(Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
        onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] strings, @NonNull int[] ints) {
        for(int i = 0; i < strings.length; i++) {
            Log.i(TAG, "result: code " + code + ", perm " + strings[i] + ", results " + ints[i]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

   @Override
    protected void onPause() {
       super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
