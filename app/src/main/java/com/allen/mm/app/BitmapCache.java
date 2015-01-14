package com.allen.mm.app;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/13
 */
public class BitmapCache {
    private MemoryCache memoryCache;
    static BitmapCache bitmapCache;

    private BitmapCache(){
            memoryCache=DefaultConfigurationFactory.createMemoryCache(2 * 1024 * 1024);
    }
    public synchronized static BitmapCache getInstance(){
        if(bitmapCache==null)
            bitmapCache=new BitmapCache();
        return  bitmapCache;
    }
    public void save(String key,Bitmap bitmap){
        LruMemoryCache lruMemoryCache= (LruMemoryCache) memoryCache;
        if(lruMemoryCache.keys().contains(key))return;
        lruMemoryCache.put(key,bitmap);
    }
    public Bitmap get(String key){
        LruMemoryCache lruMemoryCache= (LruMemoryCache) memoryCache;
        Bitmap bitmap=lruMemoryCache.get(key);
        if(bitmap!=null&&!bitmap.isRecycled()){
            return bitmap;
        }else{
            return null;
        }
    }
}
