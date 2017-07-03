package core.helpers;

public class TokenHelper {
    public static String shortToken(int byteLength) {
        java.util.Random random = new java.util.Random(System.nanoTime());
        byte[] data = new byte[byteLength];
        random.nextBytes(data);
        return new Base64().encode(data)
                .replace("=", "")
                .replace("+", "")
                .replace("/", "");
    }
}
