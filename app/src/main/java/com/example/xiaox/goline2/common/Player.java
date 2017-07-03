package com.example.xiaox.goline2.common;

import com.example.xiaox.goline2.extension.helper.ReadWriteProperty;

public class Player{
    public ReadWriteProperty<String> token;
    public ReadWriteProperty<String> name;
    public ReadWriteProperty<Integer> color;
    public Player(){
    }

    public Player(String name){
        this();
        this.name.set(name);
    }

}
