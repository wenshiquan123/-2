package com.hlzx.wenutil.http.cache;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hlzx.wenutil.utils.Logger;
import com.hlzx.wenutil.http.db.DBManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 2016/3/15.
 */
public class CacheDiskManager extends DBManager<CacheEntity>{

    private static DBManager<CacheEntity> _Instance;


    private CacheDiskManager() {
        super(new CacheDisk());
    }

    public synchronized static DBManager<CacheEntity> getInstance()
    {
        if(_Instance==null)
        {
            _Instance=new CacheDiskManager();
        }
        return _Instance;
    }

    @Override
    public List<CacheEntity> get(String querySql) {
        SQLiteDatabase execute=openReader();

        List<CacheEntity> cacheEntities=new ArrayList<CacheEntity>();
        Cursor cursor=null;
        try {
            cursor=execute.rawQuery(querySql,null);
            while (!cursor.isClosed() && cursor.moveToNext())
            {
                try {
                    CacheEntity cacheEntity = new CacheEntity();
                    int idIndex = cursor.getColumnIndex(CacheEntity.ID);
                    if (idIndex >= 0)
                        cacheEntity.setId(cursor.getInt(idIndex));

                    int keyIndex = cursor.getColumnIndex(CacheDisk.KEY);
                    if (keyIndex >= 0)
                        cacheEntity.setKey(cursor.getString(keyIndex));

                    int headIndex = cursor.getColumnIndex(CacheDisk.HEAD);
                    if (headIndex >= 0)
                        cacheEntity.setResponseHeadersJson(cursor.getString(headIndex));

                    int dataIndex = cursor.getColumnIndex(CacheDisk.DATA);
                    if (dataIndex >= 0)
                        cacheEntity.setData(cursor.getBlob(dataIndex));

                    cacheEntities.add(cacheEntity);

                }catch (Throwable e)
                {
                    Logger.w(e);
                }
            }

        }catch (Throwable e)
        {
            Logger.e(e);
        }
        readFinish(execute,cursor);

        return cacheEntities;
    }

    @Override
    public long replace(CacheEntity cacheEntity) {
        SQLiteDatabase execute=openWriter();
        ContentValues values=new ContentValues();
        values.put(CacheDisk.KEY,cacheEntity.getKey());
        values.put(CacheDisk.HEAD,cacheEntity.getResponseHeadersJson());
        values.put(CacheDisk.DATA,cacheEntity.getData());
        long id=-1;
        try {
             id=execute.replace(getTableName(),null,values);
        }catch (Throwable e)
        {

        }
        writeFinish(execute);
        return 0;
    }

    @Override
    protected String getTableName() {
        return CacheDisk.TABLE_NAME;
    }
}
