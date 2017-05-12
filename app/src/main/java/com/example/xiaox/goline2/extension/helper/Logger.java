package com.example.xiaox.goline2.extension.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaox on 2/7/2017.
 */
public class Logger {
    private StringBuilder stringBuilder = new StringBuilder();

    private void addLog(String value){
        stringBuilder.append("Logger -> " + value);
        System.out.print(value);
        onLogUpdated(Type.Add,value);
    }

    private void addLogLine(String value){
        System.out.println("Logger -> " +value);
        String _value = value + "\n";
        stringBuilder.append(_value);
        onLogUpdated(Type.Add, _value);
    }

    private void onLogUpdated(Type type, String value){
        if(this.onLogUpdatedListeners != null){
            for (OnLogUpdatedListener listener : onLogUpdatedListeners){
                listener.onLogUpdated(type, value);
            }
        }

    }

    private void clearAll(){
        logger.stringBuilder = new StringBuilder();
        onLogUpdated(Type.Clear, "");
    }

    private void writeToFile(){
    }

    public String getString(){
        return this.stringBuilder.toString();
    }

    private static final Logger logger = new Logger();
    public static void log(String value){
        logger.addLog(value);
    }
    public static void logLine(String value){
        logger.addLogLine(value);
    }

    public static void clear(){
        logger.clearAll();
    }

    public static String getLog(){
        return logger.getString();
    }
    public static Logger getLogger(){
        return logger;
    }

    private List<OnLogUpdatedListener> onLogUpdatedListeners;

    public void addLoggerListener(OnLogUpdatedListener listener){
        if(this.onLogUpdatedListeners == null)
            this.onLogUpdatedListeners = new ArrayList<>();
        this.onLogUpdatedListeners.add(listener);
    }

    public enum Type{
        Add,
        Clear
    }
    public interface OnLogUpdatedListener{
        void onLogUpdated(Type type, String value);
    }
}
