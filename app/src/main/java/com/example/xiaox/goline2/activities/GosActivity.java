package com.example.xiaox.goline2.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.xiaox.goline2.R;
import com.example.xiaox.goline2.common.ColorEx;
import com.example.xiaox.goline2.extension.helper.ActionEx;
import com.example.xiaox.goline2.extension.helper.IAction;
import com.example.xiaox.goline2.extension.view.GosViews;
import com.example.xiaox.goline2.common.GameMode;

import core.data.AILevel;
import core.data.JudgeCode;
import core.helpers.StringHelper;
import core.interfaces.IGameCoreResolver;
import core.interfaces.JudgedLiteListener;
import core.resolver.ResolveCreator;

public class GosActivity extends AppCompatActivity {
    public static final String MODE_KEY = "MODE";
    private IGameCoreResolver resolver;

    private GosViews gosViews;
    private TextView p1TV;
    private TextView p2TV;
    private Button startBtn;
    private Button undoBtn;
    private Button resetBtn;
    private int activeColor = ColorEx.ACTIVE_MID;

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.resolver.dispose();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
        gosViews = (GosViews)findViewById(R.id.gosViews);
        p1TV = (TextView)findViewById(R.id.p1TV);
        p2TV = (TextView)findViewById(R.id.p2TV);
        startBtn = (Button)findViewById(R.id.startBtn);
        undoBtn = (Button)findViewById(R.id.undoBtn);
        resetBtn = (Button)findViewById(R.id.resetBtn);
        //Get game mode
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE_KEY, GameMode.PVPLocal);
        setupPlayerAndService(mode);
    }

    private void setupActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupPlayerAndService(int mode){
        System.out.println(mode);
        switch (mode){
            case GameMode.PVE:
                resolver = ResolveCreator.BuildEVP(gosViews, AILevel.Elementary);
                break;
            case GameMode.PVPLocal:
                resolver = ResolveCreator.BuildPVP(gosViews);
                break;
            case GameMode.PVPOnline:
                break;
            default:break;
        }
        if(resolver != null){
            System.out.println("## Resolver is OK...");
            View.OnClickListener onReset =new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resolver.reset();
                }
            };
            View.OnClickListener onUndo = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resolver.undo();
                }
            };
            startBtn.setOnClickListener(onReset);
            resetBtn.setOnClickListener(onReset);
            undoBtn.setOnClickListener(onUndo);
            resolver.setJudgedListener(listener);
            resolver.ready();
            resolver.start();
        }
    }

    private JudgedLiteListener listener = new JudgedLiteListener() {
        @Override
        public void onJudged(final JudgeCode code) {
            ActionEx.invoke(new IAction() {
                @Override
                public void onAction() {
                    switch (code){
                        case Active:{
                            if(resolver.isHostActive()) updateColor(activeColor, Color.TRANSPARENT);
                            else updateColor(Color.TRANSPARENT, activeColor);
                        }break;
                        case MarkFirst:{
                            boolean isFirstHost = resolver.getFirstToken().equals(resolver.getHostToken());
                            if(isFirstHost) updateColor(activeColor, Color.TRANSPARENT);
                            else updateColor(Color.TRANSPARENT, activeColor);
                        }break;
                        case Started:{
                            String text = "Game is started!";
                            Snackbar.make(GosActivity.this.gosViews, text, Snackbar.LENGTH_SHORT).show();
                            startBtn.setEnabled(false);
                            undoBtn.setEnabled(true);
                            resetBtn.setEnabled(true);
                        }break;
                        case Ended:{
                            if(StringHelper.isNullOrEmpty(resolver.getWinnerToken())){
                                String text = "Game is ended.";
                                Snackbar.make(GosActivity.this.gosViews, text, Snackbar.LENGTH_SHORT).show();
                            }
                            startBtn.setEnabled(true);
                            undoBtn.setEnabled(false);
                            resetBtn.setEnabled(false);
                        }break;
                        case NewWinner:{
                            boolean isHost = resolver.getWinnerToken().equals(resolver.getHostToken());
                            String text = "Winner is " + (isHost ? "HOST" : "JOIN");
                            Snackbar.make(GosActivity.this.gosViews, text, Snackbar.LENGTH_SHORT).show();
                        }break;
                    }
                }
            }, invokeHandler);
        }
    };

    private Handler invokeHandler = new Handler(){
        public void handleMessage(Message message) {
            IAction action = (IAction)message.obj;
            if(action != null) action.onAction();
        }
    };

    private void updateColor(int p1Color, int p2Color){
        p1TV.setBackgroundColor(p1Color);
        p2TV.setBackgroundColor(p2Color);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
