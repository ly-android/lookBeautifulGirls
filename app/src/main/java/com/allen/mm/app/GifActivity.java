package com.allen.mm.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import org.apache.http.Header;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/14
 */
public class GifActivity extends Activity implements View.OnClickListener {

    String url;
    GifImageView imageView;
    AsyncHttpClient http;
    View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_activity);
        http=new AsyncHttpClient();
        imageView= (GifImageView) findViewById(R.id.gifImageView);
        rootView=findViewById(android.R.id.content);
        url=getIntent().getStringExtra("url");
        http.get(this, url, new BinaryHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                if(bytes!=null){
                    try {
                        GifDrawable gifDrawable=new GifDrawable(bytes);
                        if(gifDrawable!=null)
                            imageView.setImageDrawable(gifDrawable);
                        gifDrawable.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(getApplicationContext(),"加载失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        });
        rootView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
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
                GifActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.setInterpolator(new AccelerateInterpolator());
        set.setDuration(200).start();
    }
}
