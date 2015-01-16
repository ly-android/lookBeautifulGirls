package com.allen.mm.app;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import com.allen.mm.app.model.Model;
import com.allen.mm.app.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.io.File;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/4
 */
public class ImageActivity extends Activity implements PhotoViewAttacher.OnPhotoTapListener {

    private ShareActionProvider mShareActionProvider;

    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    Model model;
    DisplayImageOptions displayImageOptions;
    View rootView;
    Bitmap mBitmap;
    Intent shareIntent;
    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model=getIntent().getParcelableExtra("model");
        initActionBar();
        setContentView(R.layout.image_layout);
        rootView=findViewById(android.R.id.content);
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_photo) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.empty_photo)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.empty_photo)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .bitmapConfig(Bitmap.Config.ARGB_8888)//设置图片的解码类型//
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new SimpleBitmapDisplayer())//是否图片加载好后渐入的动画时间
                .build();//构建完成

        mImageView= (ImageView) findViewById(R.id.iv_shower);
        mAttacher=new PhotoViewAttacher(mImageView);

        ImageLoader.getInstance().loadImage(model.image_url, displayImageOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                mImageView.setImageResource(R.drawable.empty_photo);
                mAttacher.update();
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                mBitmap=bitmap;
                mImageView.setImageBitmap(bitmap);
                mAttacher.update();
                if(menu!=null)
                    setMenu(menu);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                mImageView.setImageResource(R.drawable.empty_photo);
                mAttacher.update();
            }
        });
        mAttacher.setOnPhotoTapListener(this);
    }

    private void initActionBar(){
        getActionBar().show();
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu=menu;
        return super.onCreateOptionsMenu(menu);
    }
    private void setMenu(Menu menu){
        MenuItem item=menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider)item.
                getActionProvider();
        shareIntent=createShareIntent();
        mShareActionProvider.setShareIntent(shareIntent);
    }
    /**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File file=ImageLoader.getInstance().getDiskCache().get(model.image_url);
        if(file!=null) {
            Uri uri = Uri.fromFile(file);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "更多精彩,在《天天看妹纸》");
        shareIntent.putExtra(Intent.EXTRA_TITLE, "美女");
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_share:
                Log.d("ly","分享中..");
                break;
            case R.id.action_wall:
                //设置壁纸：
                Utils.showDialog(this, "是否设为壁纸", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setWallpaper();
                    }
                },null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setWallpaper(){
        try {
            if(mBitmap==null||mBitmap.isRecycled())return;
            WallpaperManager instance = WallpaperManager.getInstance(this);
            int desiredMinimumWidth = getWindowManager().getDefaultDisplay().getWidth();
            int desiredMinimumHeight = getWindowManager().getDefaultDisplay().getHeight();
            instance.suggestDesiredDimensions(desiredMinimumWidth, desiredMinimumHeight);
            instance.setBitmap(mBitmap);
            Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPhotoTap(View view, float v, float v1) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
//        AnimatorSet set = new AnimatorSet();
//        set.playTogether(
//                ObjectAnimator.ofFloat(rootView, "scaleX", 1, 0),
//                ObjectAnimator.ofFloat(rootView, "scaleY", 1, 0),
//                ObjectAnimator.ofFloat(rootView, "alpha", 1, 0.2f)
//        );
//        set.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//               ImageActivity.super.onBackPressed();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        set.setInterpolator(new AccelerateInterpolator());
//        set.setDuration(200).start();
        super.onBackPressed();
        overridePendingTransition(0,android.R.anim.slide_out_right);
    }
}
