package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/20/2017.
 */
public class MarkExFormatException extends IllegalArgumentException {
    static final long serialVersionUID = -1L;

    public MarkExFormatException () {
        super();
    }

    public MarkExFormatException (String s) {
        super (s);
    }

    public static MarkExFormatException forInputString(String s) {
        return new MarkExFormatException("For input string: \"" + s + "\"");
    }
}