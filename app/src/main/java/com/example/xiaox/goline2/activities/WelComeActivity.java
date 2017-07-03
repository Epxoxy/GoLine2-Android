package com.example.xiaox.goline2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.xiaox.goline2.R;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.common.GameMode;

public class WelComeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getAssets();
        setContentView(R.layout.activity_wel_come);
        TextView textView = (TextView)findViewById(R.id.playTextView);
        if(textView != null) textView.setOnClickListener(startListener);
        TextView homeTitle = (TextView)findViewById(R.id.homeTitle);
        if(homeTitle != null){
            homeTitle.setOnClickListener(showLogListener);
        }
    }

    private View.OnClickListener showLogListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(WelComeActivity.this, LogActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        Logger.logLine("Start click!");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WelComeActivity.this);
        alertDialog.setTitle("Select Mode").setItems(GameMode.modeItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Logger.logLine("Dialog selected " + which);
                if(which == GameMode.PVPOnline){
                    checkLogin();
                }
                else createGosActivity(which);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }
    };

    private void checkLogin(){
        Logger.logLine("Check login!");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WelComeActivity.this);
        alertDialog.setTitle("Login").setMessage("You are not login.Login now?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent().setClass(WelComeActivity.this, LoginActivity.class));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private void createGosActivity(int mode){
        Intent intent = new Intent();
        intent.putExtra(GosActivity.MODE_KEY, mode);
        intent.setClass(WelComeActivity.this, GosActivity.class);
        startActivity(intent);
    }
}
