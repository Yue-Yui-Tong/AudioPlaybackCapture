package com.example.audioplaybackcapture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.audioplaybackcapture.log.LogHandler;
import com.example.audioplaybackcapture.service.AudioForegroundService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_CODE = 13;

    private Button startBtn;
    private Button stopBtn;

    private String [] appPermissions = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        if(v == startBtn){

            if(checkAndRequestPermissions()){
                startAudioRecord();
            }

        } else {
            stopAudioRecord();
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, AudioForegroundService.class);
        LogHandler.d(TAG, "Service is going to start");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, AudioForegroundService.class);
        LogHandler.d(TAG, "Service is going to stop");
        stopService(serviceIntent);
    }

    private void startAudioRecord(){
        startService();
        Toast.makeText(MainActivity.this, "Audio Record Started", Toast.LENGTH_SHORT).show();
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
    }

    private void stopAudioRecord(){
        stopService();
        Toast.makeText(MainActivity.this, "Audio Record Stopped", Toast.LENGTH_SHORT).show();
        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
    }

    private void goToSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + getPackageName());
        intent.setData(uri);
        startActivity(intent);
    }

    private void showPermissionRational(String permission){
        new AlertDialog.Builder(this).setTitle("Permission Denied").setMessage("Without "+ permission +" permission app is unable to record system sound.")
                .setCancelable(false)
                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkAndRequestPermissions();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void dialogForSettings(String title, String msg) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToSettings();
                        dialog.dismiss();
                    }
                }).show();
    }

    public boolean checkAndRequestPermissions(){
        List < String > listPermissionsNeeded = new ArrayList<>();
        for(String permission : appPermissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(permission);
            }
        }

        if(!listPermissionsNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            HashMap < String, Integer > permissionResults = new HashMap<>();
            int deniedCount = 0;
            for(int permissionIndx = 0; permissionIndx < permissions.length; permissionIndx++){
                if(grantResults[permissionIndx] != PackageManager.PERMISSION_GRANTED){
                    LogHandler.d(TAG, permissions[permissionIndx]);
                    permissionResults.put(permissions[permissionIndx], grantResults[permissionIndx]);
                    deniedCount++;
                }
            }
            if(deniedCount == 0){
                startAudioRecord();
            } else {
                for(Map.Entry < String, Integer > entry : permissionResults.entrySet()){
                    String permName = entry.getKey();
                    int permResult = entry.getValue();
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this, permName)){
                        showPermissionRational(permName);
                    } else {
                        dialogForSettings("Permission Denied", "Now you must allow "+ permName +" permission from settings.");
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            LogHandler.d(TAG, "Write External Storage accepted");
        }
    }
}
