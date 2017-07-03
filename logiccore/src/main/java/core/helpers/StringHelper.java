package core.helpers;
public class StringHelper {
    public static boolean isNullOrEmpty(String v){
        return v == null || v.length() <= 0;
    }
}
