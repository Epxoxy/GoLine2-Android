package com.example.xiaox.goline2.logic.basic;

/**
 * Created by xiaox on 1/29/2017.
 */
public class Spot {
    public int x;
    public int y;
    public int data;
    public Spot(){

    }
    public Spot(int x, int y, int data){
        this.x = x;
        this.y = y;
        this.data = data;
    }

    @Override
    public boolean equals(Object o){
        Spot spot = (Spot)o;
        if(spot == null) {
            return false;
        }
        else{
            return spot.x == this.x &&
                    spot.y == this.y &&
                    spot.data == this.data;
        }
    }

    @Override
    public String toString(){
        return String.format("[{0},{1}] = {2}", x, y, data);
    }
}
