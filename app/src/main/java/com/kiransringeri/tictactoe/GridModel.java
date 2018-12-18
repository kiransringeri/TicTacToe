package com.kiransringeri.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridModel {
    private Set<Integer> currentUserCells;
    private Set<Integer> otherUserCells;

    private static List<List<Integer>> winingCombinations = new ArrayList<>();
    static{
        winingCombinations.add(Arrays.asList(new Integer[]{0,1,2}));
        winingCombinations.add(Arrays.asList(new Integer[]{3,4,5}));
        winingCombinations.add(Arrays.asList(new Integer[]{6,7,8}));
        winingCombinations.add(Arrays.asList(new Integer[]{0,3,6}));
        winingCombinations.add(Arrays.asList(new Integer[]{1,4,7}));
        winingCombinations.add(Arrays.asList(new Integer[]{2,5,8}));
        winingCombinations.add(Arrays.asList(new Integer[]{0,4,8}));
        winingCombinations.add(Arrays.asList(new Integer[]{2,4,6}));
    }
    public GridModel(){
        currentUserCells = new HashSet<>();
        otherUserCells = new HashSet<>();
    }

    public boolean isCellLocked(int cellNum){
        return otherUserCells.contains(cellNum) || currentUserCells.contains(cellNum);
    }

    public void cellMarked(int cellNum, boolean currentUser){
        if(currentUser){
            currentUserCells.add(cellNum);
        }else{
            otherUserCells.add(cellNum);
        }
    }

    public boolean isGaveOver(){
//        Even hen 8 cells are filled, the game is over
        return currentUserCells.size() + otherUserCells.size() >= 8;
    }

    public boolean isCurrentUserWinner(){
        return isWinner(currentUserCells);
    }

    public boolean isOtherPersonWinner(){
        return isWinner(otherUserCells);
    }

    private boolean isWinner(Set<Integer> cells){
        if(cells.isEmpty() || cells.size() < 3){
            return false;
        }
        for(List<Integer> winningComb : winingCombinations){
            if(cells.containsAll(winningComb)){
                return true;
            }
        }
        return false;
    }
}
