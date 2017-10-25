package com.vivek.tictactoe;

import android.app.Activity;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class GameActivity extends Activity {

    /**
     * Start player. Must be 1 or 2. Default is 1.
     */
    public static final String EXTRA_START_PLAYER =
            "com.example.android.tictactoe.library.GameActivity.EXTRA_START_PLAYER";

    private static final int MSG_COMPUTER_TURN = 1;
    private static final long COMPUTER_DELAY_MS = 500;
    MyHandlerCallback myHandlerCallback = new MyHandlerCallback();
    private Handler mHandler = new Handler(myHandlerCallback);
    private Random mRnd = new Random();
    private GameView mGameView;
    private TextView mInfoView;
    private Button mButtonNext;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        /*
         * IMPORTANT: all resource IDs from this library will eventually be merged
         * with the resources from the main project that will use the library.
         *
         * If the main project and the libraries define the same resource IDs,
         * the application project will always have priority and override library resources
         * and IDs defined in multiple libraries are resolved based on the libraries priority
         * defined in the main project.
         *
         * An intentional consequence is that the main project can override some resources
         * from the library.
         * (TODO insert example).
         *
         * To avoid potential conflicts, it is suggested to add a prefix to the
         * library resource names.
         */
        setContentView(R.layout.activity_game);

        mGameView = (GameView) findViewById(R.id.game_view);
        mInfoView = (TextView) findViewById(R.id.info_turn);
        mButtonNext = (Button) findViewById(R.id.next_turn);

        mGameView.setFocusable(true);
        mGameView.setFocusableInTouchMode(true);
        mGameView.setCellListener(new MyCellListener());

        mButtonNext.setOnClickListener(new MyButtonListener());
    }

    @Override
    protected void onResume() {
        super.onResume();

        GameView.State player = mGameView.getCurrentPlayer();
        if (player == GameView.State.UNKNOWN) {
            player = GameView.State.fromInt(getIntent().getIntExtra(EXTRA_START_PLAYER, 1));
            if (!checkGameFinished(player)) {
                selectTurn(player);
            }
        }
        if (player == GameView.State.PLAYER2) {
            mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
        }
        if (player == GameView.State.WIN) {
            setWinState(mGameView.getWinner());
        }
    }


    private GameView.State selectTurn(GameView.State player) {
        mGameView.setCurrentPlayer(player);
        mButtonNext.setEnabled(false);

        if (player == GameView.State.PLAYER1) {
            mInfoView.setText(R.string.player1_turn);
            mGameView.setEnabled(true);

        } else if (player == GameView.State.PLAYER2) {
            mInfoView.setText(R.string.player2_turn);
            mGameView.setEnabled(false);
        }

        return player;
    }

    private class MyCellListener implements GameView.ICellListener {
        public void onCellSelected() {
            if (mGameView.getCurrentPlayer() == GameView.State.PLAYER1) {
                int cell = mGameView.getSelection();
                mButtonNext.setEnabled(cell >= 0);
            }
        }
    }

    private class MyButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            GameView.State player = mGameView.getCurrentPlayer();

            if (player == GameView.State.WIN) {
                GameActivity.this.finish();

            } else if (player == GameView.State.PLAYER1) {
                int cell = mGameView.getSelection();
                if (cell >= 0) {
                    mGameView.stopBlink();
                    mGameView.setCell(cell, player);
                    finishTurn();
                }
            }
        }
    }

    private class MyHandlerCallback implements KeyEvent.Callback {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_COMPUTER_TURN) {

                // Pick a non-used cell at random. That's about all the AI you need for this game.
                GameView.State[] data = mGameView.getData();
                int used = 0;
                while (used != 0x1F) {
                    int index = mRnd.nextInt(9);
                    if (((used >> index) & 1) == 0) {
                        used |= 1 << index;
                        if (data[index] == GameView.State.EMPTY) {
                            mGameView.setCell(index, mGameView.getCurrentPlayer());
                            break;
                        }
                    }
                }

                finishTurn();
                return true;
            }
            return false;
        }

        @Override
        public boolean onKeyDown(int i, KeyEvent keyEvent) {
            return false;
        }

        @Override
        public boolean onKeyLongPress(int i, KeyEvent keyEvent) {
            return false;
        }

        @Override
        public boolean onKeyUp(int i, KeyEvent keyEvent) {
            return false;
        }

        @Override
        public boolean onKeyMultiple(int i, int i1, KeyEvent keyEvent) {
            return false;
        }
    }

    private GameView.State getOtherPlayer(GameView.State player) {
        return player == GameView.State.PLAYER1 ? GameView.State.PLAYER2 : GameView.State.PLAYER1;
    }

    private void finishTurn() {
        GameView.State player = mGameView.getCurrentPlayer();
        if (!checkGameFinished(player)) {
            player = selectTurn(getOtherPlayer(player));
            if (player == GameView.State.PLAYER2) {
                mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
            }
        }
    }

    public boolean checkGameFinished(GameView.State player) {
        GameView.State[] data = mGameView.getData();
        boolean full = true;

        int col = -1;
        int row = -1;
        int diag = -1;

        // check rows
        for (int j = 0, k = 0; j < 3; j++, k += 3) {
            if (data[k] != GameView.State.EMPTY && data[k] == data[k + 1] && data[k] == data[k + 2]) {
                row = j;
            }
            if (full && (data[k] == GameView.State.EMPTY ||
                    data[k + 1] == GameView.State.EMPTY ||
                    data[k + 2] == GameView.State.EMPTY)) {
                full = false;
            }
        }

        // check columns
        for (int i = 0; i < 3; i++) {
            if (data[i] != GameView.State.EMPTY && data[i] == data[i + 3] && data[i] == data[i + 6]) {
                col = i;
            }
        }

        // check diagonals
        if (data[0] != GameView.State.EMPTY && data[0] == data[1 + 3] && data[0] == data[2 + 6]) {
            diag = 0;
        } else if (data[2] != GameView.State.EMPTY && data[2] == data[1 + 3] && data[2] == data[0 + 6]) {
            diag = 1;
        }

        if (col != -1 || row != -1 || diag != -1) {
            setFinished(player, col, row, diag);
            return true;
        }

        // if we get here, there's no winner but the board is full.
        if (full) {
            setFinished(GameView.State.EMPTY, -1, -1, -1);
            return true;
        }
        return false;
    }

    private void setFinished(GameView.State player, int col, int row, int diagonal) {

        mGameView.setCurrentPlayer(GameView.State.WIN);
        mGameView.setWinner(player);
        mGameView.setEnabled(false);
        mGameView.setFinished(col, row, diagonal);

        setWinState(player);
    }

    private void setWinState(GameView.State player) {
        mButtonNext.setEnabled(true);
        mButtonNext.setText("Back");

        String text;

        if (player == GameView.State.EMPTY) {
            text = getString(R.string.tie);
        } else if (player == GameView.State.PLAYER1) {
            text = getString(R.string.player1_win);
        } else {
            text = getString(R.string.player2_win);
        }
        mInfoView.setText(text);
    }
}
