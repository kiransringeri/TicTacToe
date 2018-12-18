package com.kiransringeri.tictactoe;

public interface GridView {
    public void markO(int cellNum);
    public void markX(int cellNum);
    public void showMessage(String message);
    public void winner();
    public void loser();
    public void tied();
    public void enableClick();
    public void disableClick();
    public void otherPlayerTurn();
}
