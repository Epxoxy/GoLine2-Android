package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/25/2017.
 */
public class ReadWriteProperty<T> extends ReadProperty<T> {

    public ReadWriteProperty(){
        super();
    }

    public ReadWriteProperty(T value){
        super(value);
    }

    public void set(T value){
        this.value = value;
    }
}
