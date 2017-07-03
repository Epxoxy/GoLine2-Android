package core.helpers;

import java.util.ArrayList;
import java.util.List;

import core.interfaces.Match;

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
