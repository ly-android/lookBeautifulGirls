package com.allen.mm.app;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.allen.mm.app.fragment.MMFragment;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;


public class MainActivity extends FragmentActivity {

    static final String TAB_MM="tab_mm";
    static final String TAB_SEX="tab_sex";
    static final String TAB_SIWA="tab_siwa";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private boolean drawerArrowColor;
    String[] values = new String[]{
            "美女",
            "性感",
            "丝袜",
            "段子"
    };
    MenuAdapter menuAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setTitle("天天看妹纸");
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
//                | ActionBar.DISPLAY_SHOW_TITLE);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_main);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);


        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawerArrow, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        menuAdapter=new MenuAdapter();
        mDrawerList.setAdapter(menuAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
//                        mDrawerToggle.setAnimateEnabled(false);
//                        drawerArrow.setProgress(1f);
                        show(0);
                        break;
                    case 1:
                        show(1);
                        break;
                    case 2:
                        show(2);
                        break;
                    case 3:
//                        if (drawerArrowColor) {
//                            drawerArrowColor = false;
//                            drawerArrow.setColor(R.color.ldrawer_color);
//                        } else {
//                            drawerArrowColor = true;
//                            drawerArrow.setColor(R.color.drawer_arrow_second_color);
//                        }
//                        mDrawerToggle.syncState();
                        break;
                }

            }
        });
        show(0);
    }
    Fragment currFragment;
    private void show(int position){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        Fragment fragment=null;
        if(currFragment!=null)
            transaction.hide(currFragment);
        switch (position){
            case 0:
                fragment=getSupportFragmentManager().findFragmentByTag(TAB_MM);
                if(fragment!=null){
                    transaction.show(fragment);
                }else{
                    fragment= MMFragment.instance(MMFragment.TAB_MM);
                    transaction.add(R.id.container,fragment,TAB_MM);
                }
                break;
            case 1:
                fragment=getSupportFragmentManager().findFragmentByTag(TAB_SEX);
                if(fragment!=null){
                    transaction.show(fragment);
                }else{
                    fragment= MMFragment.instance(MMFragment.TAB_XG);
                    transaction.add(R.id.container,fragment,TAB_SEX);
                }
                break;
            case 2:
                fragment=getSupportFragmentManager().findFragmentByTag(TAB_SIWA);
                if(fragment!=null){
                    transaction.show(fragment);
                }else{
                    fragment= MMFragment.instance(MMFragment.TAB_SW);
                    transaction.add(R.id.container,fragment,TAB_SIWA);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
        currFragment=fragment;
        mDrawerLayout.closeDrawer(mDrawerList);
        menuAdapter.setChoice(position);
        getActionBar().setTitle(values[position]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    class MenuAdapter extends BaseAdapter{

        int choice=0;
        public void setChoice(int pos){
            choice=pos;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return values.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null)
                convertView=getLayoutInflater().inflate(R.layout.menu_item,null);
            TextView tv= (TextView) convertView.findViewById(R.id.tv);
            tv.setText(values[position]);
            if(choice==position)
                convertView.setBackgroundColor(Color.parseColor("#cccccc"));
            else
                convertView.setBackgroundColor(android.R.color.transparent);
            return convertView;
        }
    }
}
