package com.example.xiaox.goline2.extension.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 1/31/2017.
 */
public class Filter {
    /**
     *For generate collection filter
     *When not going to use java8
     **/
    public static <T> List<T> filter(List<T> list, Match<T> match){
        List<T> result = new ArrayList<T>();
        for(T value : list){
            if(match.validate(value)){
                result.add(value);
            }
        }
        return result;
    }
}
