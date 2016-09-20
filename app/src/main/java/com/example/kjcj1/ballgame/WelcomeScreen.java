package com.example.kjcj1.ballgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class WelcomeScreen extends ActionBarActivity {

    public final static String DIFFICULTY_MESSAGE = "Default";
    private RelativeLayout relLayout;
    private TextView playerScoreTextView;
    private String playerScoreMessage = "9000";
    private SeekBar seekBar;
    private int difficultyValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        relLayout = new RelativeLayout(this);
        relLayout.setBackgroundColor(Color.CYAN);

        getSupportActionBar().hide();

        //Turn keep screenon OFF
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int oldHighScore = prefs.getInt("key", 9000); //9000 is the default value

        seekBar = (SeekBar)findViewById(R.id.difficultyBar);
        seekBar.setProgress(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser)
            {
                difficultyValue = progressValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {}
        });

        if(getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE) != null)
        {
            playerScoreMessage = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
            //Log.e("playerScore getIntent", playerScoreMessage);

            if(Integer.parseInt(playerScoreMessage) > oldHighScore)
            {
                editor.putInt("key", Integer.parseInt(playerScoreMessage));
                editor.commit();
            }
            else
            {
                editor.putInt("key", oldHighScore);
                editor.commit();
            }
        }

        playerScoreTextView = (TextView)findViewById(R.id.textViewMainScore);

        if(playerScoreTextView != null)
        {
            if(Integer.parseInt(playerScoreMessage) > oldHighScore)
            {
                playerScoreTextView.setText("Highscore: " + playerScoreMessage);
            }
            else
            {
                playerScoreTextView.setText("Highscore: " + oldHighScore);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void gameStart(View view)
    {
        //Log.e("Difficulty value", "Diffculty value: " + difficultyValue);
        Intent intent = new Intent(this, MainActivity.class);
        String toSend = "" + difficultyValue;
        intent.putExtra(DIFFICULTY_MESSAGE, toSend);
        startActivity(intent);
    }

}
