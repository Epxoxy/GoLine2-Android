package com.example.xiaox.goline2.extension.helper;

/**
 * Created by xiaox on 1/31/2017.
 */
public class DateTimeHelper {

    public static String msToShortString(long _milliseconds){
        int[] data = calculateTime(_milliseconds);
        return String.format("%02d:%02d:%02d", data[0], data[1], data[2]);
    }
    public static String millisecondToString(long _milliseconds){
        int[] data = calculateTime(_milliseconds);
        return String.format("%02d:%02d:%02d:%03d", data[0], data[1], data[2], data[3]);
    }
    public static String millisecondToSplitString(long _milliseconds){
        int[] data = calculateTime(_milliseconds);
        return String.format("%02d-%02d-%02d-%03d", data[0], data[1], data[2], data[3]);
    }

    public static int[] calculateTime(long _milliseconds){
        long totalTime = _milliseconds;
        int milliseconds = 0;
        int seconds = 0;
        int minutes = 0;
        int hours = 0;
        while(totalTime >= 1000){
            totalTime -= 1000;
            ++seconds;
        }
        while(seconds >= 60){
            seconds -= 60;
            ++minutes;
        }
        while(minutes >= 60){
            minutes -= 60;
            ++hours;
        }
        milliseconds = (int)totalTime;
        return new int[]{ hours, minutes, seconds, milliseconds};
    }
}
