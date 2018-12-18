package com.kiran.tictactoe;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GridView {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Map<Integer, TextView> cellNumberMap;
    private Map<Integer, Integer> cellId2NumberMap;

    private GridPresenter presenter = null;

    private View.OnClickListener clickHandler = null;
    private View.OnClickListener signInSignOutHandler = null;
    private TextView messageView = null;

    private boolean clickEnabled = true;
    private MediaPlayer loserSound = null;
    private MediaPlayer winnerSound = null;
    private MediaPlayer gameOverSound = null;
    private MediaPlayer startGameSound = null;
    private MediaPlayer waitForOtherSound = null;
    private MediaPlayer yourTurnSound = null;
    private MediaPlayer nowPlaying = null;

    private Button loginBtn;
    private Button logoutBtn;
    private TextView userDetailTxt;

    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cellNumberMap = new HashMap<>();
        cellId2NumberMap = new HashMap<>();
        presenter = new GridPresenter(this);

        userDetailTxt = findViewById(R.id.user_detail);
        loginBtn = findViewById(R.id.login_btn);
        logoutBtn = findViewById(R.id.logout_button);

        signInSignOutHandler = new LoginLogoutClickHandler();

        loginBtn.setOnClickListener(signInSignOutHandler);
        logoutBtn.setOnClickListener(signInSignOutHandler);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initGame();
    }

    private GoogleSignInAccount signedInAccount;
    private GoogleSignInClient mGoogleSignInClient;

    private void checkLoggedInUser(){
        Log.i(TAG,"Inside checkLoggedInUser");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null){
            Log.i(TAG,"User not signed in");
            signInSilently();
        }else{
            Log.i(TAG, "User signed in: "+account.getEmail());
            logoutBtn.setVisibility(View.VISIBLE);
            updateUI(account);
        }
    }

    private void signInSilently(){
        Log.i(TAG,"Inside signInSilently");
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        Log.i(TAG,"Inside onComplete, success="+task.isSuccessful());
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount account = task.getResult();
                            logoutBtn.setVisibility(View.VISIBLE);
                            Log.i(TAG,"User signed in: "+account.getEmail());
                            updateUI(account);
                        } else {
                            // Player will need to sign-in explicitly using via UI
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,"Inside onActivityResult, requestCode="+requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                logoutBtn.setVisibility(View.VISIBLE);
                updateUI(account);
            }catch(Throwable th){
                String message = "Error in signing in";
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
                Log.e(TAG,"Exception. ", th);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLoggedInUser();
    }

    private void updateUI(GoogleSignInAccount user){
        if(user == null){
            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            userDetailTxt.setText("Please login to play with another user");
        }else{
            userDetailTxt.setText("You are logged in as " + user.getGivenName());
            loginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);
        }
        signedInAccount = user;
//        TODO: If user logged in now, then need to clear the board and start again
    }

    private void initGame(){
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

    private class LoginLogoutClickHandler implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.login_btn:
                    Intent intent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(intent, RC_SIGN_IN);
                    break;
                case R.id.logout_button:
                    signOut();
                    break;
            }
        }
    }

    private void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageView.setText("Congratulations! You won.");
                playMedia(winnerSound);
                restart();
            }
        });
    }

    @Override
    public void loser(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageView.setText("Bad luck! You lost. Better luck next time");
                playMedia(loserSound);
                restart();
            }
        });
    }

    @Override
    public void tied(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageView.setText("Its a tie! Neither of you won. Try to win next time.");
                playMedia(gameOverSound);
                restart();
            }
        });
    }

    @Override
    public void enableClick(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clickEnabled = true;
                messageView.setText("The other person has completed his move. Now its your turn");
                playMedia(yourTurnSound);
            }
        });
    }

    @Override
    public void disableClick(){
        clickEnabled = false;
    }

    @Override
    public void otherPlayerTurn(){
        playMedia(waitForOtherSound);
    }

    private void playMedia(MediaPlayer mp){
        if(nowPlaying != null && nowPlaying.isPlaying()){
            nowPlaying.pause();
        }
        nowPlaying = mp;
        mp.seekTo(0);
        mp.start();
    }

}
