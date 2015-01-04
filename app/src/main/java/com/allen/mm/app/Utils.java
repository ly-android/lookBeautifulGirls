package com.allen.mm.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/4
 */
public class Utils {
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        }
        return ni.isConnected()
                || (ni.isAvailable() && ni.isConnectedOrConnecting());
    }
}
