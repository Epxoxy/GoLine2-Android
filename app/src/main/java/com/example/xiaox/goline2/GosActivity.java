package com.example.xiaox.goline2;

import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.xiaox.goline2.extension.event.IEventHandler;
import com.example.xiaox.goline2.extension.helper.DateTimeHelper;
import com.example.xiaox.goline2.extension.helper.Logger;
import com.example.xiaox.goline2.extension.helper.StopWatch;
import com.example.xiaox.goline2.extension.view.Ellipse;
import com.example.xiaox.goline2.extension.view.GosViews;
import com.example.xiaox.goline2.logic.basic.ActionType;
import com.example.xiaox.goline2.logic.basic.ChessGameMode;
import com.example.xiaox.goline2.logic.player.AIPlayer;
import com.example.xiaox.goline2.logic.player.Player;
import com.example.xiaox.goline2.logic.service.ActivatedChangedEventArgs;
import com.example.xiaox.goline2.logic.service.ChessGameService;
import com.example.xiaox.goline2.logic.service.ChessGameState;
import com.example.xiaox.goline2.logic.service.GameActionEventArgs;
import com.example.xiaox.goline2.logic.service.GameStateEventArgs;
import com.example.xiaox.goline2.service.SFXService;

public class GosActivity extends AppCompatActivity {
    public static final String MODE_KEY = "MODE";
    private int skyBlue = Color.parseColor("#87CEEB");
    private ChessGameService chessGameService = new ChessGameService();
    private GosViews gosViews;
    private TextView timeTextView;
    private TextView p1TimeTextView;
    private TextView p2TimeTextView;
    GosActivity gosActivity = null;
    private Player operator;
    private Player player02;

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.chessGameService.detach();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button tipsNewestBtn = (Button)findViewById(R.id.tipsNewest);
        tipsNewestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newestColRow != null)
                    gosViews.tips(newestColRow[0], newestColRow[1]);
            }
        });

        setupActionBar();
        setupBasicHandler();
        setupShowTimeHandler();
        gosViews = (GosViews)findViewById(R.id.gosViews);
        if(gosViews != null) {
            gosViews.setOnLatticeClickListener(gosViewLatticeClick);
        }
        timeTextView = (TextView)findViewById(R.id.timeTextView);
        p1TimeTextView = (TextView)findViewById(R.id.p1TimeTextView);
        p2TimeTextView = (TextView)findViewById(R.id.p2TimeTextView);
        gosActivity = this;
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE_KEY, ChessGameMode.PVPLocal);
        setupPlayerAndService(mode);
    }

    private void setupActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupPlayerAndService(int mode){
        switch (mode){
            case ChessGameMode.AIvsAI:
                operator = new AIPlayer();
                player02 = new AIPlayer();
                break;
            case ChessGameMode.PVE:
                operator = new Player("Operator");
                player02 = new AIPlayer();
                break;
            case ChessGameMode.PVPOnline:
                operator = new Player("Operator");
                player02 = new Player("Joiner");
                break;
            case ChessGameMode.PVPLocal:
            default:
                operator = new Player("Operator");
                player02 = new Player("Joiner");
                break;
        }
        operator.color.set(skyBlue);
        player02.color.set(Color.GRAY);
        chessGameService.onStateChanged.addHandler(ctlShowTimeHandler);
        chessGameService.onActivatedChanged.addHandler(showPlayerTimeHandler);
        chessGameService.onAction.addHandler(gameActionEventHandler);
        chessGameService.join(operator);
        chessGameService.join(player02);
        chessGameService.setBeginner(operator);
        player02.sendAction(ActionType.Ready);
        if(operator.isAI()) operator.sendAction(ActionType.Ready);
        //player01.sendAction(ActionType.Ready);
    }

    private IEventHandler<GameStateEventArgs> ctlShowTimeHandler;
    private IEventHandler<ActivatedChangedEventArgs> showPlayerTimeHandler;

    private boolean pauseShowP1 = false;
    private boolean pauseShowP2 = false;
    private boolean pauseShowTotal = false;
    private Handler p1Handler;
    private Handler p2Handler;
    private Handler showTotalTimeHandler;

    private Integer[] newestColRow;
    private IEventHandler<GameActionEventArgs> gameActionEventHandler;
    private GosViews.OnLatticeClickListener gosViewLatticeClick;
    private void setupBasicHandler(){
        gameActionEventHandler = new IEventHandler<GameActionEventArgs>() {
            @Override
            public void onEvent(Object sender, GameActionEventArgs args) {
                if(args.getActionType() == ActionType.Input){
                    Logger.logLine("Get input when " + System.nanoTime());
                    Integer[] col_row = (Integer[])args.getParams();
                    newestColRow = col_row;
                    final Ellipse ellipse = new Ellipse(gosActivity);
                    ellipse.setFill(args.getPlayer().color.get());
                    gosViews.addViewIn(ellipse, col_row[0], col_row[1]);
                    SFXService.play(GosActivity.this, R.raw.click2);
                    ellipse.setScaleX(1.5f);
                    ellipse.setScaleY(1.5f);
                    ellipse.setAlpha(0.5f);
                    ellipse.animate().scaleX(1).scaleY(1).alpha(1).setDuration(100).start();
                }
            }
        };
        gosViewLatticeClick = new GosViews.OnLatticeClickListener(){
            @Override
            public void onLatticeClick(int column, int row, int clickRadius) {
                if(chessGameService.getState() == ChessGameState.Ended){
                    if(!chessGameService.getBeginner().isAI()){
                        chessGameService.getBeginner().sendAction(ActionType.Ready);
                    }
                }
                if(chessGameService.getState() == ChessGameState.Started){
                    Player activated = chessGameService.getActivatedPlayer();
                    if(activated.isAI()) return;
                    activated.input(column, row);
                }
            }
        };
    }

    private void setupShowTimeHandler(){
        ctlShowTimeHandler = new IEventHandler<GameStateEventArgs>() {
            @Override
            public void onEvent(Object sender, GameStateEventArgs args) {
                if (args.getNewState() == ChessGameState.Started) {
                    pauseShowTotal = false;
                    showTotalTimeHandler.sendEmptyMessage(0);
                } else {
                    pauseShowTotal = true;
                }
            }
        };
        showPlayerTimeHandler = new IEventHandler<ActivatedChangedEventArgs>() {
            @Override
            public void onEvent(Object sender, ActivatedChangedEventArgs args) {
                if(args.getActivatedPlayer() == operator){
                    pauseShowP1 = false;
                    pauseShowP2 = true;
                    p1Handler.sendEmptyMessage(0);
                    Logger.logLine("player01");
                }else{
                    pauseShowP1 = true;
                    pauseShowP2 = false;
                    p2Handler.sendEmptyMessage(0);
                    Logger.logLine("player02");
                }
            }
        };
        showTotalTimeHandler = new Handler(){
            public void handleMessage(Message message){
                switch (message.what){
                    case 0:
                        showTotalTimeHandler.removeMessages(0);
                        if(!pauseShowTotal){
                            timeTextView.setText(DateTimeHelper.msToShortString(chessGameService.getRunningTime()));
                            showTotalTimeHandler.sendEmptyMessageDelayed(0, 500);
                        }
                        break;
                    case 1: showTotalTimeHandler.removeMessages(0);break;
                    default:break;
                }
            }
        };
        p1Handler = new Handler(){
            public void handleMessage(Message message){
                switch (message.what){
                    case 0:
                        p1Handler.removeMessages(0);
                        if(!pauseShowP1){
                            p1TimeTextView.setText(DateTimeHelper.msToShortString(chessGameService.getUsedTimeOf(operator)));
                            p1Handler.sendEmptyMessageDelayed(0, 500);
                        }
                        break;
                    case 1:
                        p1Handler.removeMessages(0);
                        break;
                    default:break;
                }
            }
        };
        p2Handler = new Handler(){
            public void handleMessage(Message message){
                switch (message.what){
                    case 0:
                        p2Handler.removeMessages(0);
                        if(!pauseShowP2){
                            p2TimeTextView.setText(DateTimeHelper.msToShortString(chessGameService.getUsedTimeOf(player02)));
                            p2Handler.sendEmptyMessageDelayed(0, 500);
                        }
                        break;
                    case 1:
                        p2Handler.removeMessages(0);
                        break;
                    default:break;
                }
            }
        };
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
