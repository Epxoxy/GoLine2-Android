package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/25/2017.
 */
public class ReadProperty<T> {

    protected T value;

    protected ReadProperty(){

    }

    public ReadProperty(T value){
        this.value = value;
    }

    public T get(){
        return value;
    }
}
