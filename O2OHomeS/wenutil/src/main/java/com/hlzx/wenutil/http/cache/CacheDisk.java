package com.hlzx.wenutil.http.cache;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hlzx.wenutil.http.NoHttp;
import com.hlzx.wenutil.http.db.Field;

/**
 * Created by alan on 2016/3/15.
 */
public class CacheDisk extends SQLiteOpenHelper implements Field {

    public static final String DB_CACHE_NAME="nohttp_cache_db.db";
    public static final int DB_CACHE_VERSION=1;

    public static final String TABLE_NAME = "cache_table";
    public static final String KEY = "key";
    public static final String HEAD = "head";
    public static final String DATA = "data";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE cache_table(_id INTEGER PRIMARY KEY AUTOINCREMENT, key TEXT, head TEXT, data BLOB)";
    private static final String SQL_CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX cache_unique_index ON cache_table(\"key\")";
    private static final String SQL_DELETE_TABLE = "DROP TABLE cache_table";
    private static final String SQL_DELETE_UNIQUE_INDEX = "DROP UNIQUE INDEX cache_unique_index";

    public CacheDisk() {
        super(NoHttp.getContext(), DB_CACHE_NAME, null, DB_CACHE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.beginTransaction();
        try
        {
            db.execSQL(SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_UNIQUE_INDEX);
            db.setTransactionSuccessful();
        }finally {
             db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(newVersion!=oldVersion)
        {
            db.beginTransaction();
            try {
                db.execSQL(SQL_DELETE_TABLE);
                db.execSQL(SQL_DELETE_UNIQUE_INDEX);
                db.execSQL(SQL_CREATE_TABLE);
                db.execSQL(SQL_CREATE_UNIQUE_INDEX);
                db.setTransactionSuccessful();

            }finally {
                db.endTransaction();
            }
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }
}
