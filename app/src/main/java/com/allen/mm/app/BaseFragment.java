package com.allen.mm.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/15
 */
public class BaseFragment extends Fragment{
    protected float density=1;
    protected int width;
    protected int height;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        width=getResources().getDisplayMetrics().widthPixels;
        height=getResources().getDisplayMetrics().heightPixels;
        density=getResources().getDisplayMetrics().density;
        Log.d("ly","density="+density);
    }
}
