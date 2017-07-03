package com.example.xiaox.goline2.common;

/**
 * Created by xiaox on 2/4/2017.
 */
public class OnlineAccount {
    public String token;
    public int id;
    public String userName;
    public String nickName;
    public AccountState state;
    public ScoreData scoreData;
}

enum AccountState{
    None,
    Offline,
    Online,
    OnlineGaming
}