package com.example.xiaox.goline2.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xiaox on 1/22/2017.
 */
public class SqlHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "";
    private static final String TABLE_NAME = "";
    private SQLiteDatabase db;
    public SqlHelper(Context context){
        super(context, DB_NAME, null, 2);
    }

    public void insert(ContentValues values){
        db.insert(TABLE_NAME, "nullColumnHack", values);
    }

    public Cursor query(String sqlString){
        return db.query(TABLE_NAME, new String[]{"columns"},"selection",new String[]{"selectionArgs"},"groupBy","Having", "oriderBy");
    }

    public void delete(int id){
        db.delete(TABLE_NAME, "whereClause", new String[]{"WhereArgs"});
    }

    public void close(){
        if(this.db != null) db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        this.db = db;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
