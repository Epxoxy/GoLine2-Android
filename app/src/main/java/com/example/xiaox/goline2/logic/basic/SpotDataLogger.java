package com.example.xiaox.goline2.logic.basic;

import android.support.annotation.NonNull;

import com.example.xiaox.goline2.logic.basic.Point;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

/**
 * Created by xiaox on 1/29/2017.
 */
public class SpotDataLogger {
    //Final
    public final static int UNREACHABLE = 0;
    public final static int REACHABLE = 1;
    private final static int MARK_START = 2;
    //Private member
    private int[][] defEntry;
    private int[][] entry = null;
    private WeakHashMap<Integer,Stack<Point>> inputsStack;
    private int reachableCount;
    private Stack<Spot> undoStack;
    private Stack<Spot> redoStack;

    public SpotDataLogger(int[][] defEntry){
        this.defEntry = new int[defEntry.length][defEntry[0].length];
        for(int i = 0; i < defEntry.length; i++)
        {
            for(int j = 0; j < defEntry[i].length; j++)
            {
                this.defEntry[i][j] = defEntry[i][j];
            }
        }
        this.init();
    }
    public SpotDataLogger(int[][] defEntry, int reachableMark){
        this.defEntry = new int[defEntry.length][defEntry[0].length];
        for(int i = 0; i < defEntry.length; i++)
        {
            for(int j = 0; j < defEntry[i].length; j++)
            {
                this.defEntry[i][j] = defEntry[i][j] == reachableMark ? REACHABLE : UNREACHABLE;
            }
        }
        this.init();
    }
    public SpotDataLogger(boolean[][] defBoolEntry){
        this.defEntry = new int[defBoolEntry.length][defBoolEntry[0].length];
        for(int i = 0; i < defBoolEntry.length; i++)
        {
            for(int j = 0; j < defBoolEntry[i].length; j++)
            {
                this.defEntry[i][j] = defBoolEntry[i][j] ? REACHABLE : UNREACHABLE;
            }
        }
        this.init();
    }

    private void init(){
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.inputsStack = new WeakHashMap<>();
        this.reachableCount = 0;
        this.entry = new int[this.defEntry.length][this.defEntry[0].length];
        for(int i = 0; i < this.defEntry.length; i++)
        {
            for(int j = 0; j < this.defEntry[i].length; j++)
            {
                this.entry[i][j] = this.defEntry[i][j];
                if(this.entry[i][j] == REACHABLE) ++ this.reachableCount;
            }
        }
    }

    //Debug option
    private void printEntry(){
        for(int i = 0; i < entry.length; i++)
        {
            for(int j = 0; j < entry[i].length; j++)
            {
                System.out.print(entry[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Method group
     * Internal occupy method group
     * */
    private boolean unsetOccupy(int x, int y){
        if(isInRange(x,y)){
            if(this.entry[x][y] == UNREACHABLE || this.entry[x][y] == REACHABLE) return false;
            int mark = entry[x][y];
            this.entry[x][y] = REACHABLE;
            ++reachableCount;
            //Update inputs
            Stack<Point> inputs = inputsStack.get(mark);
            if(inputs == null) inputs = new Stack<>();
            else inputs.pop();
            inputsStack.put(mark, inputs);
            return true;
        }
        return false;
    }
    private boolean setOccupy(int x, int y, int mark){
        if(isFill() == false && isInRange(x,y)){
            if(this.entry[x][y] == REACHABLE){
                this.entry[x][y] = mark;
                --reachableCount;
                //Update inputs
                Stack<Point> inputs = inputsStack.get(mark);
                if(inputs == null) inputs = new Stack<>();
                inputs.push(new Point(x, y));
                inputsStack.put(mark, inputs);
                return true;
            }
        }
        return false;
    }


    /********* Method group start ******
     ********** Public easy access *******/

    //Part one : verity access

    public boolean isFill(){
        return reachableCount <= 0;
    }
    public boolean isInRange(int x, int y){
        return entry.length > x && entry[0].length > y;
    }
    public boolean canLogAt(int x, int y){
        return isInRange(x, y) && entry[x][y] == REACHABLE;
    }
    public boolean isOccupiedBy(int x, int y, int mark){
        return isInRange(x, y) && entry[x][y] == mark;
    }

    //Part two : others

    public int[][] copyEntry(){
        int[][] copy = new int[this.entry.length][this.entry[0].length];
        for(int i = 0; i < this.entry.length; i++)
        {
            for(int j = 0; j < this.entry[i].length; j++)
            {
                copy[i][j] = this.entry[i][j];
            }
        }
        return copy;
    }

    public Stack<Point> getInputsOf(int mark){
        if(inputsStack.containsKey(mark))return inputsStack.get(mark);
        return  null;
    }

    private boolean revokeLog(int x, int y){
        return this.unsetOccupy(x, y);
    }

    public boolean log(int x, int y, int mark){
        boolean success = this.setOccupy(x, y, mark);
        if(success) pushUndoClearRedo(new Spot(x, y, mark));
        return success;
    }

    private boolean logWithNotClearRedo(int x, int y, int mark) {
        return this.setOccupy(x, y, mark);
    }

    public int generateMark(int index){
        if(index < 0)
            throw new IndexOutOfBoundsException("Index for crating mark must large than -1");
        return index + MARK_START;
    }

    public int countOccupiedOf(int mark){
        if(this.inputsStack.containsKey(mark)){
            return this.inputsStack.get(mark).size();
        }
        return 0;
    }

    public int countOccupiedOf(int mark, Point...points){
        int count = 0;
        for(Point point : points){
            if(isInRange(point.x, point.y)){
                if(this.entry[point.x][point.y] == mark) ++count;
            }
        }
        return count;
    }

    public List<Point> availablePoints(){
        List<Point> availablePoints = new ArrayList<>();
        for(int i = 0; i < this.entry.length; i++)
        {
            for(int j = 0; j < this.entry[i].length; j++)
            {
                if(this.entry[i][j] == REACHABLE)
                    availablePoints.add(new Point(i, j));
            }
        }
        return availablePoints;
    }

    public void reset(){
        this.init();
    }

    /******* Method group end **************/


    /******* Method group start ******
    ********** Stack operation *******/

    public boolean canUndo(){
        return this.undoStack.isEmpty() == false;
    }

    public boolean canRedo(){
        return this.redoStack.isEmpty() == false;
    }

    public Spot undo(){
        if(this.undoStack.isEmpty()) return null;
        Spot undSpot = this.undoStack.pop();
        this.revokeLog(undSpot.x, undSpot.y);
        this.redoStack.push(undSpot);
        return undSpot;
    }

    public Spot redo(){
        if(this.redoStack.isEmpty()) return null;
        Spot redoSpot = this.redoStack.pop();
        this.logWithNotClearRedo(redoSpot.x, redoSpot.y, redoSpot.data);
        this.undoStack.push(redoSpot);
        return redoSpot;
    }

    public Spot getRedoTop(){
        if(this.redoStack.isEmpty()) return null;
        return this.redoStack.peek();
    }

    public Spot getUndoTop(){
        if(undoStack.isEmpty()) return null;
        return undoStack.peek();
    }

    private void pushUndoClearRedo(Spot spot){
        this.undoStack.push(spot);
        this.redoStack.clear();
    }

    /********* Method group end *********/

}
