package com.allen.mm.app.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/4
 */
public class Utils {

    public static AlertDialog showDialog(Context context,String message,DialogInterface.OnClickListener okListener,DialogInterface.OnClickListener cancelListener){
        AlertDialog dialog=null;
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        dialog=builder.setMessage(message).setPositiveButton("确定", okListener).setNegativeButton("取消",cancelListener).create();
        dialog.show();
        return dialog;
    }

    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        }
        return ni.isConnected()
                || (ni.isAvailable() && ni.isConnectedOrConnecting());
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
