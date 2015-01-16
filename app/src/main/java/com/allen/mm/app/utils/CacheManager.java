package com.allen.mm.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.*;

/**
 * 描述:manager cache
 *
 * @author: liyong on 2015/1/5
 */
public class CacheManager {
    public static final String TAG="CacheManager";

    public static CacheManager instance=null;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private Handler mUIHandler;

    private CacheManager() {
        mHandlerThread=new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler=new Handler(mHandlerThread.getLooper());
        mUIHandler=new Handler(Looper.getMainLooper());
    }

    public static CacheManager getInstance(){
        if(instance==null){
            synchronized (CacheManager.class) {
                if(instance==null)
                    instance = new CacheManager();
            }
        }
        return instance;
    }

    public interface CacheHandler<T extends Serializable>{
        public void onSuccess(T object);
        public void onFailure(Exception e);
    }

    public void getAsync(final Context context,final String key,final CacheHandler handler){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final Serializable object=get(context,key);
                    if(object!=null){
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                handler.onSuccess(object);
                            }
                        });
                    }
                } catch (final Exception e) {
                    Log.e(TAG,"getAsync->error:"+e.getMessage());
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.onFailure(e);
                        }
                    });
                }
            }
        });
    }

    public <T extends Serializable> void putAsync(final Context context,final String key,final T object,final CacheHandler handler){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    put(context,key,object);
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.onSuccess(object);
                        }
                    });
                } catch (final Exception e) {
                    Log.e(TAG,"getAsync->error:"+e.getMessage());
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.onFailure(e);
                        }
                    });
                }
            }
        });
    }


    public  <T extends Serializable> void  put(Context context, String key, T object){
        try{
            FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
            fos.close();
        }catch (Exception e){
            Log.e(TAG,"put->error:"+e.getMessage());
            e.printStackTrace();
        }
    }

    public  <T extends Serializable>  T get(Context context, String key){
        T object=null;
        try{
            FileInputStream fis = context.openFileInput(key);
            if(fis==null) return null;
            ObjectInputStream ois = new ObjectInputStream(fis);
            object = (T) ois.readObject();
        }catch (Exception e){
            Log.e(TAG,"get->error:"+e.getMessage());
            e.printStackTrace();
        }
        return object;
    }

    public void destory(){
        mUIHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }
}
