package com.example.xiaox.goline2.extension.helper;

import android.accounts.AccountManager;
import android.util.Base64;

import java.net.Authenticator;
import java.util.Random;
import java.util.UUID;

/**
 * Created by xiaox on 1/30/2017.
 */
public class Token {
    public static String shortToken(int byteLength){
        java.util.Random random = new java.util.Random(System.nanoTime());
        byte[] data = new byte[byteLength];
        random.nextBytes(data);
        return Base64.encodeToString(data, Base64.NO_WRAP)
                .replace("=", "")
                .replace("+", "")
                .replace("/", "");
    }
}
