package com.allen.mm.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.allen.mm.app.fragment.GifFragment;
import com.allen.mm.app.fragment.MMFragment;
import com.allen.mm.app.fragment.VideoFragment;
import com.allen.mm.app.utils.ClientFactory;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;


public class MainActivity extends BaseActivity {

    static final String TAB_MM="tab_mm";
    static final String TAB_SEX="tab_sex";
    static final String TAB_SIWA="tab_siwa";
    static final String TAB_DZ="tab_DZ";
    static final String TAB_SP="tab_sp";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;
    private boolean drawerArrowColor;

    String[] values = new String[]{
            "美女",
            "性感",
            "丝袜",
            "段子",
            "视频"
    };
    MenuAdapter menuAdapter;
    LinearLayout right_menu;
    TextView tv_change_skin;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getSupportActionBar().setTitle("天天看妹纸");
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
//                | ActionBar.DISPLAY_SHOW_TITLE);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        right_menu= (LinearLayout) findViewById(R.id.right_menu);
        tv_change_skin= (TextView) findViewById(R.id.tv_change_skin);
        tv_change_skin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog!=null){
                    dialog.show();
                }else
                    dialog=showItems();
            }
        });

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
        mDrawerToggle.setDrawerLeftView(mDrawerList);
        mDrawerToggle.setDrawerRightView(right_menu);
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
                        show(0,TAB_MM,MMFragment.instance(MMFragment.TAB_MM));
                        break;
                    case 1:
                        show(1,TAB_SEX,MMFragment.instance(MMFragment.TAB_XG));
                        break;
                    case 2:
                        show(2,TAB_SIWA,MMFragment.instance(MMFragment.TAB_SW));
                        break;
                    case 3:
                        show(3,TAB_DZ,GifFragment.instance());
                        break;
                    case 4:
                        show(4,TAB_SP,VideoFragment.instance());
                        break;
                }

            }
        });
        show(0,TAB_MM,MMFragment.instance(MMFragment.TAB_MM));
    }


    private AlertDialog showItems(){
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("选择主题").setItems(skins, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                ClientFactory.notifyClients(IThemeUpdateListener.class, new ClientFactory.IClientCall<IThemeUpdateListener>() {
                    @Override
                    public void onCall(IThemeUpdateListener obj) {
                        obj.update(which);
                    }
                });
            }
        }).create();
        dialog.show();
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    Fragment currFragment;
    private void show(int position,String tag,BaseFragment showFragment){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        Fragment fragment=null;
        if(currFragment!=null){
            currFragment.onPause();
            transaction.hide(currFragment);
        }
        fragment=getSupportFragmentManager().findFragmentByTag(tag);
        if(fragment!=null){
            transaction.show(fragment);
            fragment.onResume();
        }else{
            fragment= showFragment;
            transaction.add(R.id.container,fragment,tag);
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
        }else if(item.getItemId()==R.id.action_setting){
            if(mDrawerLayout.isDrawerOpen(right_menu))
                mDrawerLayout.closeDrawer(right_menu);
            else
                mDrawerLayout.openDrawer(right_menu);
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
