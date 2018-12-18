package com.kiransringeri.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GridPresenter {
    private GridView view;
    private GridModel model;

    public GridPresenter(GridView view){
        this.view = view;
        this.model = new GridModel();
    }
    public void cellClicked(int cellNum){
//        User clicked this cell
//        Mark this cell as "X"
        view.disableClick();

        view.markX(cellNum);
        model.cellMarked(cellNum, true);
//        After current user moves, check whether he is a winner or not
        if(model.isCurrentUserWinner()){
            view.winner();
            return;
        }

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                //        TODO: Check if the current user is the winner
                int cellNum = otherPlayerMove();
                if(cellNum != -1) {
                    view.otherPlayerTurn();

                    try {
                        Thread.sleep(1000*2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    model.cellMarked(cellNum, false);
                    view.markO(cellNum);
//          Check if the other user is the winner
                    if(model.isOtherPersonWinner()){
                        view.loser();
                        return;
                    }
                }
//        We come here when none of the users is a winner
//        So, if game is over, then it is a tie
                if(model.isGaveOver()){
                    view.tied();
                }
                else{
//            Else allow user to put a X in next free cell
                    view.enableClick();
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }

    public boolean isCellLocked(int cellNum){
        return model.isCellLocked(cellNum);
    }

    private int otherPlayerMove(){
        List<Integer> cellNumList = new ArrayList<>();
        for(int i=0; i < 9; i++){
            if(!model.isCellLocked(i)){
                cellNumList.add(i);
            }
        }
        if(cellNumList.isEmpty())
            return -1;
        Random rand = new Random();
        int pos = rand.nextInt(cellNumList.size());
        return cellNumList.get(pos);
    }
}
