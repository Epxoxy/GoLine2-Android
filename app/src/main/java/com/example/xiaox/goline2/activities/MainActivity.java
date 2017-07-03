package com.example.xiaox.goline2.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.xiaox.goline2.R;
import com.example.xiaox.goline2.extension.helper.AnimationEX;
import com.example.xiaox.goline2.extension.view.Circle;
import com.example.xiaox.goline2.extension.view.SwitchView;
import com.example.xiaox.goline2.extension.view.TextCircle;
import com.example.xiaox.goline2.extension.helper.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private int skyBlue = Color.parseColor("#87CEEB");
    private MainActivity mainActivity = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        /*ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        mainActivity = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(fab != null){
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Logger.clear();
                }
            });
        }

        Circle strokeCircle =(Circle)findViewById(R.id.strokeEllipse);
        TextCircle textCircle = (TextCircle)findViewById(R.id.textCircle);
        setupEllipses(strokeCircle,textCircle);

        View logRoot = (View)findViewById(R.id.logViewRoot);
        TextView logView = (TextView)findViewById(R.id.homeLogTextView);
        SwitchView logSwitch = (SwitchView)findViewById(R.id.logSwitch);
        setupLogSwitch(logRoot, logView, logSwitch);
    }

    private void setupLogSwitch(final View logRoot, final TextView logView, SwitchView logSwitch){
        if(logView != null){
            logView.setText(Logger.getLog());
            Logger.getLogger().addLoggerListener(new Logger.OnLogUpdatedListener() {
                @Override
                public void onLogUpdated(Logger.Type type, String value) {
                    if(type == Logger.Type.Add){
                        logView.setText(Logger.getLog());
                    }else{
                        logView.setText("");
                    }
                }
            });
        }
        if(logSwitch != null){
            logRoot.setVisibility(logSwitch.isOpened() ? View.VISIBLE : View.INVISIBLE);
            logSwitch.setOnStateChangedListener(new SwitchView.OnStateChangedListener() {
                @Override
                public void toggleToOn(boolean isOpened) {
                    logRoot.setVisibility(isOpened ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }
    }

    private void setupEllipses(Circle strokeCircle, TextCircle textCircle){
        if(strokeCircle == null || textCircle == null) return;
        strokeCircle.setFill(Color.TRANSPARENT);
        strokeCircle.setStrokeFill(skyBlue);
        strokeCircle.setStroke(4);
        strokeCircle.setScaleOnTouch(false);
        textCircle.setStroke(4);
        textCircle.setStrokeFill(skyBlue);
        textCircle.setFill(Color.LTGRAY);
        AnimationSet aniSet = new AnimationSet(true);
        ScaleAnimation scale = AnimationEX.CreateScale(1,1.5f,1,1.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0.1f);
        scale.setRepeatCount(Animation.INFINITE);
        scale.setRepeatMode(Animation.RESTART);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.RESTART);
        scale.setDuration(1500);
        alphaAnimation.setDuration(1500);
        aniSet.addAnimation(scale);
        aniSet.addAnimation(alphaAnimation);
        aniSet.setStartOffset(500);
        strokeCircle.startAnimation(aniSet);
        textCircle.setOnLongClickListener(holdListener);
    }

    private View.OnLongClickListener holdListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_login){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.action_log){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, LogActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
        @Override
        public void onBackPressed() {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_camera) {
                // Handle the camera action
            } else if (id == R.id.nav_gallery) {

            } else if (id == R.id.nav_slideshow) {

            } else if (id == R.id.nav_manage) {

            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
}
