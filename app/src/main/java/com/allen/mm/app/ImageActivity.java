package com.allen.mm.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/4
 */
public class ImageActivity extends Activity implements PhotoViewAttacher.OnPhotoTapListener {
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    Model model;
    DisplayImageOptions displayImageOptions;
    View rootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        model=getIntent().getParcelableExtra("model");
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
                  mImageView.setImageBitmap(bitmap);
                  mAttacher.update();
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                mImageView.setImageResource(R.drawable.empty_photo);
                mAttacher.update();
            }
        });
        mAttacher.setOnPhotoTapListener(this);
    }

   private void animatFinish(){
       AnimatorSet set = new AnimatorSet();
       set.playTogether(
               ObjectAnimator.ofFloat(rootView, "scaleX", 1, 0),
               ObjectAnimator.ofFloat(rootView, "scaleY", 1, 0),
               ObjectAnimator.ofFloat(rootView, "alpha", 1, 0.2f)
       );
       set.addListener(new Animator.AnimatorListener() {
           @Override
           public void onAnimationStart(Animator animation) {

           }

           @Override
           public void onAnimationEnd(Animator animation) {
               onBackPressed();
           }

           @Override
           public void onAnimationCancel(Animator animation) {

           }

           @Override
           public void onAnimationRepeat(Animator animation) {

           }
       });
       set.setInterpolator(new AccelerateInterpolator());
       set.setDuration(500).start();
   }

    @Override
    public void onPhotoTap(View view, float v, float v1) {
        animatFinish();
    }
}
