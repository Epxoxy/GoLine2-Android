package com.example.xiaox.goline2.extension.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xiaox on 1/20/2017.
 */
public class DrawPoint implements Cloneable {
    public float x;
    public float y;
    public MarkEx mark;
    static String pattern = "([A-Z])(.*),(.*)";

    public DrawPoint(float x, float y) {
        this.x = x;
        this.y = y;
        mark = MarkEx.L;
    }

    public DrawPoint(MarkEx mark, float x, float y) {
        this.x = x;
        this.y = y;
        this.mark = mark;
    }

    public DrawPoint(MarkEx mark, float value) {
        this.mark = mark;
        switch (mark) {
            case H: {
                this.x = value;
            }
            break;
            case V: {
                this.y = value;
            }
            break;
        }
    }

    @Override
    public DrawPoint clone(){
        DrawPoint p = new DrawPoint(this.x, this.y);
        p.mark = this.mark;
        return p;
    }

    public static DrawPoint parseDrawPoint(String value) throws MarkExFormatException {
        if(value.contains(",")){
            Pattern r = Pattern.compile(pattern);
            Matcher match = r.matcher(value);
            if(match.find()){
                if(match.groupCount() != 3)
                    throw MarkExFormatException.forInputString(value);
                String[] items = new String[3];
                for(int i = 0; i < items.length; i++){
                    items[i] = match.group(i + 1);//i+ 1 to jump group zero
                }
                MarkEx mark = Enum.valueOf(MarkEx.class, items[0]);
                float x = Float.parseFloat(items[1]);
                float y = Float.parseFloat(items[2]);
                return new DrawPoint(mark,x,y);
            }else{
                throw MarkExFormatException.forInputString(value);
            }
        }else{
            String[] items = new String[2];
            items[0] = String.valueOf(value.charAt(0));
            items[1] = value.replace(items[0], "");
            float v = Float.parseFloat(items[1]);
            MarkEx mark = Enum.valueOf(MarkEx.class, items[0]);
            return new DrawPoint(mark,v);
        }
    }

    public static List<DrawPoint> parseDrawPoints(String value) throws MarkExFormatException{
        List<DrawPoint>  pointList = new ArrayList<DrawPoint>();
        String upperString = (value +" ").toUpperCase();
        String patternItems = "([A-Z].+?)\\s";
        Pattern r = Pattern.compile(patternItems);
        Matcher m = r.matcher(upperString);
        DrawPoint prePoint = null;
        while (m.find()){
            DrawPoint p = DrawPoint.parseDrawPoint(m.group());
            switch (p.mark){
                case H:{
                    if(prePoint != null) p.y = prePoint.y;
                    else throw MarkExFormatException.forInputString("where mark before \"H"+ p.y+ "\" of " +value);
                }break;
                case V:{
                    if(prePoint != null) p.x = prePoint.x;
                    else throw MarkExFormatException.forInputString("where mark before \"H"+ p.x+ "\" of "+value);
                }break;
            }
            prePoint = p;
            pointList.add(p);
        }
        return pointList;
    }

    @Override
    public String toString(){
        return mark+ ""+x +","+y;
    }
}
