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
import com.example.audioplaybackcapture.permission.PermissionManager;
import com.example.audioplaybackcapture.service.AudioForegroundService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = MainActivity.class.getSimpleName();
    private final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12;

    private Button startBtn;
    private Button stopBtn;

    private PermissionManager permissionManager;

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

        permissionManager = new PermissionManager(this);
    }

    @Override
    public void onClick(View v) {
        if(v == startBtn){
            permissionManager.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionManager.PermissionAskListener() {
                @Override
                public void onNeedPermission() {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }

                @Override
                public void onPermissionPreviouslyDenied() {
                    showPermissionRational();
                }

                @Override
                public void onPermissionPreviouslyDeniedWithNeverAskAgain() {
                    dialogForSettings("Permission Denied", "Now you must allow write external storage from settings.");
                }

                @Override
                public void onPermissionGranted() {
                    startAudioRecord();
                }
            });
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

    private void showPermissionRational(){
        new AlertDialog.Builder(this).setTitle("Permission Denied").setMessage("Without this permission app is unable to record system sound.")
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
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogHandler.d(TAG, "Write External Storage accepted");
                    startAudioRecord();
                }
                break;
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
