package com.mbixtech.wordquizgame.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "game.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "scores";
    public static final String COL_ID = "_id";
    public static final String COL_SCORE = "score";
    public static final String COL_DIFFICULTY = "difficulty";

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating table");

        String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SCORE + " REAL, "
                + COL_DIFFICULTY + " INTEGER" + ")";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
