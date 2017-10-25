package com.vivek.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.start_player).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startGame(true);
                    }
                });

        findViewById(R.id.start_comp).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        startGame(false);
                    }
                });
    }

    private void startGame(boolean startWithHuman) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.EXTRA_START_PLAYER,
                startWithHuman ? GameView.State.PLAYER1.getValue() : GameView.State.PLAYER2.getValue());
        startActivity(i);
    }
}
