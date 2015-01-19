package com.ikimuhendis.ldrawer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.allen.mm.app.R;

import java.lang.reflect.Method;


public class ActionBarDrawerToggle extends android.support.v4.app.ActionBarDrawerToggle {

    private static final String TAG = ActionBarDrawerToggle.class.getName();

    protected Activity mActivity;
    protected DrawerLayout mDrawerLayout;

    protected int mOpenDrawerContentDescRes;
    protected int mCloseDrawerContentDescRes;
    protected DrawerArrowDrawable mDrawerImage;
    protected boolean animateEnabled;

    protected boolean drawerToggleLeft=true,drawerToggleRight=false;
    protected View drawerLeftView,drawerRightView;

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    public ActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, DrawerArrowDrawable drawerImage, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, R.drawable.ic_drawer, openDrawerContentDescRes, closeDrawerContentDescRes);
        mActivity = activity;
        mDrawerLayout = drawerLayout;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        mDrawerImage = drawerImage;
        animateEnabled = true;
    }

    public void syncState() {
        if (mDrawerImage == null) {
            super.syncState();
            return;
        }
        if (animateEnabled) {
            if(isDrawerToggleLeft()&&drawerLeftView!=null){
                if (mDrawerLayout.isDrawerOpen(drawerLeftView)) {
                    mDrawerImage.setProgress(1.f);
                } else {
                    mDrawerImage.setProgress(0.f);
                }
            }else if(isDrawerToggleRight()&&drawerRightView!=null){
                if (mDrawerLayout.isDrawerOpen(drawerRightView)) {
                    mDrawerImage.setProgress(1.f);
                } else {
                    mDrawerImage.setProgress(0.f);
                }
            }
        }
        setActionBarUpIndicator();
        setActionBarDescription();
    }

    public void setDrawerIndicatorEnabled(boolean enable) {
        if (mDrawerImage == null) {
            super.setDrawerIndicatorEnabled(enable);
            return;
        }
        setActionBarUpIndicator();
        setActionBarDescription();
    }

    public boolean isDrawerIndicatorEnabled() {
        if (mDrawerImage == null) {
            return super.isDrawerIndicatorEnabled();
        }
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (mDrawerImage == null) {
            super.onConfigurationChanged(newConfig);
            return;
        }
        syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        if (mDrawerImage == null) {
            super.onDrawerSlide(drawerView, slideOffset);
            return;
        }
        if (animateEnabled) {
            mDrawerImage.setVerticalMirror(!mDrawerLayout.isDrawerOpen(GravityCompat.START));
            if(drawerView==drawerLeftView){
//                Log.w("ly","drawer left");
                if(isDrawerToggleLeft())
                {
                    mDrawerImage.setProgress(slideOffset);
                }
            }else if(drawerView==drawerRightView){
//                Log.w("ly","drawer left");
                if(isDrawerToggleRight())
                {
                    mDrawerImage.setProgress(slideOffset);
                }
            }
        }
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if (mDrawerImage == null) {
            super.onDrawerOpened(drawerView);
            return;
        }
        if (animateEnabled) {
            if(drawerView==drawerLeftView){
                if(isDrawerToggleLeft())
                {
                    mDrawerImage.setProgress(1.f);
                }
            }else if(drawerView==drawerRightView){
                if(isDrawerToggleRight())
                {
                    mDrawerImage.setProgress(1.f);
                }
            }
        }
        setActionBarDescription();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (mDrawerImage == null) {
            super.onDrawerClosed(drawerView);
            return;
        }
        if (animateEnabled) {
            if(drawerView==drawerLeftView){
                if(isDrawerToggleLeft())
                {
                    mDrawerImage.setProgress(0.f);
                }
            }else if(drawerView==drawerRightView){
                if(isDrawerToggleRight())
                {
                    mDrawerImage.setProgress(0.f);
                }
            }
        }
        setActionBarDescription();
    }

    protected void setActionBarUpIndicator() {
        if (mActivity != null) {
            try {
                Method setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod("setHomeAsUpIndicator",
                    Drawable.class);
                setHomeAsUpIndicator.invoke(mActivity.getActionBar(), mDrawerImage);
                return;
            } catch (Exception e) {
                Log.e(TAG, "setActionBarUpIndicator error", e);
            }

            final View home = mActivity.findViewById(android.R.id.home);
            if (home == null) {
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == android.R.id.home ? second : first;

            if (up instanceof ImageView) {
                ImageView upV = (ImageView) up;
                upV.setImageDrawable(mDrawerImage);
            }
        }
    }

    protected void setActionBarDescription() {
        if (mActivity != null && mActivity.getActionBar() != null) {
            try {
                Method setHomeActionContentDescription = ActionBar.class.getDeclaredMethod(
                    "setHomeActionContentDescription", Integer.TYPE);
                setHomeActionContentDescription.invoke(mActivity.getActionBar(),
                    mDrawerLayout.isDrawerOpen(GravityCompat.START) ? mOpenDrawerContentDescRes : mCloseDrawerContentDescRes);
                if (Build.VERSION.SDK_INT <= 19) {
                    mActivity.getActionBar().setSubtitle(mActivity.getActionBar().getSubtitle());
                }
            } catch (Exception e) {
                Log.e(TAG, "setActionBarUpIndicator", e);
            }
        }
    }

    public void setAnimateEnabled(boolean enabled) {
        this.animateEnabled = enabled;
    }

    public boolean isAnimateEnabled() {
        return this.animateEnabled;
    }

    public void setActionBarDrawerToggleEnable(boolean left,boolean right){
        drawerToggleLeft=left;
        drawerToggleRight=right;
    }

    public boolean isDrawerToggleLeft() {
        return drawerToggleLeft;
    }

    public boolean isDrawerToggleRight() {
        return drawerToggleRight;
    }

    public void setDrawerLeftView(View drawerLeftView) {
        this.drawerLeftView = drawerLeftView;
    }

    public void setDrawerRightView(View drawerRightView) {
        this.drawerRightView = drawerRightView;
    }

    public View getDrawerLeftView() {
        return drawerLeftView;
    }

    public View getDrawerRightView() {
        return drawerRightView;
    }
}
