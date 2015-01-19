package com.allen.mm.app;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.allen.mm.app.utils.ClientFactory;
import com.allen.mm.app.utils.PreferenceUtil;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/16
 */
public class BaseActivity extends FragmentActivity implements IThemeUpdateListener{

    public static final String  KEY_THEME="KEY_THEME";
    protected String[] skins = new String[] { "粉红", "淡蓝", "雅黑", "橘黄","深紫" };
    protected int []skin_colors=new int[]{
            R.color.skin_fenhong,
            R.color.skin_danlan,
            R.color.skin_yahei,
            R.color.skin_juhuang,
            R.color.skin_shenzi
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClientFactory.addClients(this);
        //加载皮肤:
        int theme= PreferenceUtil.readInt(this, KEY_THEME);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(skin_colors[theme])));
        setContentView(R.layout.activity_main);
    }

    protected void setSkinTheme(int index){
        PreferenceUtil.write(this,KEY_THEME,index);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(skin_colors[index])));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClientFactory.removeClients(this);
    }

    @Override
    public void update(int index) {
        setSkinTheme(index);
    }
}
