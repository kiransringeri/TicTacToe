package com.kiransringeri.tictactoe;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements GridView {

    private Map<Integer, TextView> cellNumberMap;
    private Map<Integer, Integer> cellId2NumberMap;

    private GridPresenter presenter = null;

    private View.OnClickListener clickHandler = null;
    private TextView messageView = null;

    private boolean clickEnabled = true;
    private MediaPlayer loserSound = null;
    private MediaPlayer winnerSound = null;
    private MediaPlayer gameOverSound = null;
    private MediaPlayer startGameSound = null;
    private MediaPlayer waitForOtherSound = null;
    private MediaPlayer yourTurnSound = null;
    private MediaPlayer nowPlaying = null;

    private void playMedia(MediaPlayer mp){
        if(nowPlaying != null && nowPlaying.isPlaying()){
            nowPlaying.pause();
        }
        nowPlaying = mp;
        mp.seekTo(0);
        mp.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cellNumberMap = new HashMap<>();
        cellId2NumberMap = new HashMap<>();
        presenter = new GridPresenter(this);

        messageView = findViewById(R.id.message);

        loserSound = MediaPlayer.create(this, R.raw.loser);
        winnerSound = MediaPlayer.create(this, R.raw.winner);
        gameOverSound = MediaPlayer.create(this, R.raw.tie);
        startGameSound = MediaPlayer.create(this, R.raw.game_start);
        waitForOtherSound = MediaPlayer.create(this, R.raw.wait_for_other);
        yourTurnSound = MediaPlayer.create(this, R.raw.your_turn);

        clickHandler = new CellClickHandler();

        for(int i=0; i < 9; i++){
            String cellId = "cell_"+i;
            int resID = getResources().getIdentifier(cellId, "id", getPackageName());
            TextView txtView = findViewById(resID);
            cellNumberMap.put(i, txtView);
            cellId2NumberMap.put(resID, i);
            txtView.setOnClickListener(clickHandler);
        }

        messageView.setText("Click any cell to start");
        playMedia(startGameSound);
    }

    private void restart(){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                clickEnabled = true;
                presenter = new GridPresenter(MainActivity.this);
                for(TextView txt  :cellNumberMap.values()){
                    txt.setText("");
                }
                messageView.setText("Click any cell to start");
                playMedia(startGameSound);
            }
        }, 1000*4);

    }
    private class CellClickHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(!clickEnabled){
                return;
            }
            TextView txt = (TextView)v;
            int cellNum = cellId2NumberMap.get(txt.getId());
            messageView.setText("Great! Now wait for the other person's move");
            presenter.cellClicked(cellNum);
        }
    }

    @Override
    public void markX(int cellNum){
        TextView txt = cellNumberMap.get(cellNum);
        txt.setTextColor(getResources().getColor(R.color.colorCurrentPlayer, getTheme()));
        txt.setText("X");
    }

    @Override
    public void markO(int cellNum){
        TextView txt = cellNumberMap.get(cellNum);
        txt.setTextColor(getResources().getColor(R.color.colorOtherPlayer, getTheme()));
        txt.setText("O");
    }

    @Override
    public void showMessage(String message){
        messageView.setText(message);
    }

    @Override
    public void winner(){
        messageView.setText("Congradulations! You won.");
        playMedia(winnerSound);
        restart();
    }

    @Override
    public void loser(){
        messageView.setText("Bad luck! You lost. Better luck next time");
        playMedia(loserSound);
        restart();
    }

    @Override
    public void tied(){
        messageView.setText("Its a tie! Neither of you lost. Try to win next time.");
        playMedia(gameOverSound);
        restart();
    }

    @Override
    public void enableClick(){
        clickEnabled = true;
        messageView.setText("The other person has completed his move. Now its your turn");
        playMedia(yourTurnSound);
    }

    @Override
    public void disableClick(){
        clickEnabled = false;
    }

    @Override
    public void otherPlayerTurn(){
        playMedia(waitForOtherSound);
    }
}
