package com.kiransringeri.tictactoe;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cellNumberMap = new HashMap<>();
        cellId2NumberMap = new HashMap<>();
        presenter = new GridPresenter(this);

        messageView = findViewById(R.id.message);

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
        messageView.setText("The other person has completed his move. Now its your turn");
    }

    @Override
    public void showMessage(String message){
        messageView.setText(message);
    }

    @Override
    public void winner(){
        messageView.setText("Congradulations! You won.");
        restart();
    }

    @Override
    public void loser(){
        messageView.setText("Bad luck! You lost. Better luck next time");
        restart();
    }

    @Override
    public void tied(){
        messageView.setText("Its a tie! Neither of you lost. Try to win next time.");
        restart();
    }

    @Override
    public void enableClick(){
        clickEnabled = true;
    }

    @Override
    public void disableClick(){
        clickEnabled = false;
    }
}
