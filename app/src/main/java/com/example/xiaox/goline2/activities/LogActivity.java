package com.example.xiaox.goline2.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.xiaox.goline2.R;
import com.example.xiaox.goline2.extension.helper.Logger;

public class LogActivity extends AppCompatActivity {
    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setupActionBar();
        textView = (TextView)findViewById(R.id.logTextView);
        Logger.getLogger().addLoggerListener(logUpdatedListener);
        textView.setText(Logger.getLog());
        Button clearBtn = (Button)findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.clear();
            }
        });
    }

    private Logger.OnLogUpdatedListener logUpdatedListener = new Logger.OnLogUpdatedListener() {
        @Override
        public void onLogUpdated(Logger.Type type, String value) {
            if(textView != null){
                if(type == Logger.Type.Add){
                    textView.setText(Logger.getLog());
                }else{
                    textView.setText("");
                }
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Logs");
        }
    }
}
