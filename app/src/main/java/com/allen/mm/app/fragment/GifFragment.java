package com.allen.mm.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.allen.mm.app.*;
import com.allen.mm.app.model.GifModel;
import com.allen.mm.app.model.Model;
import com.allen.mm.app.utils.Utils;
import com.baoyz.widget.PullRefreshLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import java.util.ArrayList;

/**
 * 描述:gif 布局
 *
 * @author: liyong on 2015/1/13
 */
public class GifFragment extends BaseFragment implements PullRefreshLayout.OnRefreshListener {
    DisplayImageOptions displayImageOptions;
    PullRefreshLayout layout;
    RecyclerView recyclerView;
    int page = 1;
    int pageSize = 20;
    AsyncHttpClient httpClient;
    ArrayList<GifModel> list;
    GifAdapter gifAdapter;

    public static GifFragment instance() {
        return new GifFragment();
    }

    public GifFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpClient = new AsyncHttpClient();
        list = new ArrayList<GifModel>();
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_photo) //设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.empty_photo)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.empty_photo)  //设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new FadeInBitmapDisplayer(100))//是否图片加载好后渐入的动画时间
                .build();//构建完成
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.giffragment_layout, null);
        layout = (PullRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        layout.setOnRefreshListener(this);
        layout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        gifAdapter = new GifAdapter();
        recyclerView.setAdapter(gifAdapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        ImageLoader.getInstance().resume();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        ImageLoader.getInstance().pause();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        ImageLoader.getInstance().pause();
                        break;
                    default:
                        break;
                }
                //加载更多
                if (!layout.isRefreshing()
                        && recyclerView.getAdapter().getItemCount() == linearLayoutManager.findLastVisibleItemPosition() + 1
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    page += 1;
                    getData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (gifAdapter.mHolder != null)
                    if (linearLayoutManager.findFirstVisibleItemPosition() > gifAdapter.mHolder.pos || gifAdapter.mHolder.pos > linearLayoutManager.findLastVisibleItemPosition()) {
                        if (gifAdapter.mHolder.iv.getDrawable() instanceof GifDrawable) {
                            GifDrawable drawable = (GifDrawable) gifAdapter.mHolder.iv.getDrawable();
                            if (drawable.isPlaying())
                                drawable.stop();
                        }
                    }
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            outState.putParcelableArrayList("list", list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList tempList = savedInstanceState.getParcelableArrayList("list");
            if (tempList != null) {
                this.list.clear();
                this.list.addAll(tempList);
                recyclerView.getAdapter().notifyDataSetChanged();
            } else {
                onRefresh();
            }
        } else
            onRefresh();
    }

    @Override
    public void onRefresh() {
        page = 1;
        layout.setRefreshing(true);
        getData();
    }

    private void getData() {
        httpClient.get(getActivity(), String.format(Config.API_DZ, page, pageSize), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                layout.setRefreshing(false);
                int preLength = list.size();
                if (response != null) {
                    try {
                        JSONArray imags = response.getJSONArray("list");
                        if (imags != null) {
                            if (page == 1) {
                                list.clear();
                                preLength = 0;
                            }
                            for (int i = 0; i < imags.length(); i++) {
                                JSONObject object = imags.getJSONObject(i);
                                GifModel model = new GifModel();
                                model.text = object.optString("text");
                                model.gifFistFrame = object.getString("gifFistFrame");
                                model.height = object.getInt("height");
                                model.width = object.getInt("width");
                                model.image0 = object.getString("image0");
                                model.image1 = object.getString("image1");
                                model.image2 = object.getString("image2");
                                model.is_gif = object.getInt("is_gif");

                                list.add(model);
                                if (preLength == 0)
                                    recyclerView.getAdapter().notifyDataSetChanged();
                                else
                                    recyclerView.getAdapter().notifyItemInserted(preLength - 1 + i);
                            }
//                            recyclerView.getAdapter().notifyItemRangeInserted(preLength,imags.length());

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                layout.setRefreshing(false);
                Log.e("MainActivity", throwable.getMessage());
                Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_LONG).show();
            }
        });
    }

    class GifAdapter extends RecyclerView.Adapter<GifAdapter.Holder> {

        byte[] intentBytes = null;
        GifAdapter.Holder mHolder;

        @Override
        public GifAdapter.Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(getActivity()).inflate(R.layout.card_layout, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final GifAdapter.Holder viewHolder, int i) {
            final GifModel model = list.get(i);
            viewHolder.tv.setText(model.text);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.iv.getLayoutParams();
            int w = width - Utils.dip2px(getActivity(), 30);
            params.height = w * model.height / model.width;
            viewHolder.iv.setLayoutParams(params);
            String imagUrl = null;
            viewHolder.pos = i;
            if (model.is_gif == 0) {
                imagUrl = TextUtils.isEmpty(model.image0) ? model.image1 : model.image2;
                ImageLoader.getInstance().displayImage(imagUrl, viewHolder.iv, displayImageOptions);
            } else {
                imagUrl = model.gifFistFrame;
//                viewHolder.iv.startAnimation();
                httpClient.get(getActivity(), model.image0, new BinaryHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        if (bytes != null) {
                            intentBytes = bytes;
                            try {
                                mHolder = viewHolder;
                                GifDrawable gifDrawable = new GifDrawable(bytes);
                                if (gifDrawable != null) {
                                    viewHolder.iv.setImageDrawable(gifDrawable);
                                    gifDrawable.start();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                    }
                });
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ly","onClick--model.is_gif="+model.is_gif);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    Intent intent = null;
                    if (model.is_gif == 0) {
                        intent = new Intent(getActivity(), ImageActivity.class);
                        Model model1 = new Model();
                        model1.image_url = model.image0;
                        intent.putExtra("model", (Parcelable) model1);
                    } else {
                        intent = new Intent(getActivity(), GifActivity.class);
                        intent.putExtra("url", model.image0);
                    }
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeScaleUpAnimation(v, x / 2, y / 2, v.getWidth(), v.getHeight());
                    ActivityCompat.startActivity(getActivity(), intent, optionsCompat.toBundle());
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            GifImageView iv;
            TextView tv;
            int pos;

            public Holder(View itemView) {
                super(itemView);
                iv = (GifImageView) itemView.findViewById(R.id.iv_card);
                tv = (TextView) itemView.findViewById(R.id.tv_title);
            }
        }
    }
}
