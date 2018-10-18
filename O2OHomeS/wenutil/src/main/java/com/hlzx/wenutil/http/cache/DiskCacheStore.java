package com.hlzx.wenutil.http.cache;

import com.hlzx.wenutil.http.db.DBManager;
import com.hlzx.wenutil.http.db.Where;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 2016/3/15.
 */
public enum DiskCacheStore implements Cache<CacheEntity> {
    INSTANCE;

    /**
     * Database sync lock.
     */
    private Lock mLock;
    /**
     * Database manager.
     */
    private DBManager<CacheEntity> mManager;



    DiskCacheStore()
    {
        mLock=new ReentrantLock();
        mManager=CacheDiskManager.getInstance();
    }

    @Override
    public CacheEntity get(String key) {
        mLock.lock();
        try {
            Where where=new Where(CacheDisk.KEY, Where.Options.EQUAL,key);
            List<CacheEntity> cacheEntities=mManager.get(CacheDisk.ALL,where.get(), null, null, null);
            return cacheEntities.size()>0 ? cacheEntities.get(0) :null;
        }finally {
            mLock.unlock();
        }
    }

    @Override
    public CacheEntity replace(String key, CacheEntity entrance) {
        mLock.lock();
        try {
            entrance.setKey(key);
            mManager.replace(entrance);
            return entrance;
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean remove(String key) {
        if (key == null)
            return true;
        mLock.lock();
        try {
            Where where = new Where(CacheDisk.KEY, Where.Options.EQUAL, key);
            return mManager.delete(where.toString());
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean clear() {
        mLock.lock();
        try {
            return mManager.deleteAll();
        } finally {
            mLock.unlock();
        }
    }
}
