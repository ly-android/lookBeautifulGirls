package com.allen.mm.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    public static float density=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("天天看妹纸");
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        density=getResources().getDisplayMetrics().density;
        Log.d("MainActivity","density="+density);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(2 * 1024 * 1024)
                .discCacheSize(50 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();//开始构建
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

        DisplayImageOptions displayImageOptions;
        SwipeRefreshLayout refreshLayout;
        RecyclerView recyclerView;

        int page=0;
        int pageSize=30;
        AsyncHttpClient httpClient;

        ArrayList<Model> list;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            httpClient=new AsyncHttpClient();
            list=new ArrayList<Model>();

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
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            initViews(rootView);
            return rootView;
        }

        private void initViews(View rootView) {
            refreshLayout= (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
            refreshLayout.setColorSchemeColors(android.R.color.holo_blue_dark, android.R.color.holo_purple,
                    android.R.color.holo_orange_dark, android.R.color.holo_green_light);
            refreshLayout.setOnRefreshListener(this);

            recyclerView= (RecyclerView) rootView.findViewById(R.id.recyclerView);

            // 交错网格布局管理器
           final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
//            staggeredGridLayoutManager.offsetChildrenHorizontal(10);
//            staggeredGridLayoutManager.offsetChildrenVertical(10);
            staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            // 设置布局管理器
            recyclerView.setLayoutManager(staggeredGridLayoutManager);

            recyclerView.setAdapter(new ImageAdapter());

            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    //加载更多
                    int into[]=new int[2];
                    staggeredGridLayoutManager.findLastVisibleItemPositions(into);
                    if (!refreshLayout.isRefreshing()
                            && recyclerView.getAdapter().getItemCount() == Math.max(into[0],into[1])+1
                            && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        page++;
                        getData();
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                    int into[]=new int[2];
                    staggeredGridLayoutManager
                            .findFirstCompletelyVisibleItemPositions(into);
                    refreshLayout.setEnabled(into[0] == 0||into[1]==0);
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            if(savedInstanceState!=null){
                ArrayList tempList=savedInstanceState.getParcelableArrayList("list");
                if(tempList!=null){
                    this.list=savedInstanceState.getParcelableArrayList("list");
                    recyclerView.getAdapter().notifyDataSetChanged();
                }else{
                    load();
                }
            }else
                load();
        }

        private void load(){
            if(Utils.isNetworkAvailable(getActivity())){
                onRefresh();
            }else{
                Toast.makeText(getActivity(),"加载失败,请检查网络",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelableArrayList("list",list);
        }

        @Override
        public void onRefresh() {
            //TODO 刷新
            page=0;
            getData();
        }
        private void getData(){
            httpClient.get(getActivity(), String.format(Config.API_MM, page, pageSize),new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    refreshLayout.setRefreshing(false);
                    if(response!=null){
                        try {
                            JSONArray imags=response.getJSONArray("data");
                            if(imags!=null){
                                if(page==1){
                                    list.clear();
                                }
                                for (int i = 0; i < imags.length(); i++) {
                                    JSONObject object=imags.getJSONObject(i);
                                    Model model=new Model();
                                    model.desc=object.getString("desc");
                                    model.image_url=object.getString("image_url");
                                    model.thumb_large_url=object.getString("thumb_large_url");
                                    model.thumbnail_url=object.getString("thumbnail_url");
                                    list.add(model);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    refreshLayout.setRefreshing(false);
                    Log.e("MainActivity",throwable.getMessage());
                    Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_LONG).show();
                }
            });
        }
        class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder>{

            @Override
            public ImageAdapter.Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
                return new Holder(LayoutInflater.from(getActivity()).inflate(R.layout.item,viewGroup,false));
            }

            @Override
            public void onBindViewHolder(final ImageAdapter.Holder viewHolder, int i) {
                Model model=list.get(i);
                viewHolder.tv.setText(model.desc);
                String imagUrl=density>=2?model.thumb_large_url:model.thumbnail_url;
                ImageLoader.getInstance().loadImage(imagUrl,displayImageOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) viewHolder.iv.getLayoutParams();
                        params.height=bitmap.getHeight();
//                        Log.e("MainActivity","height="+params.height);
                        viewHolder.iv.setLayoutParams(params);
                        viewHolder.iv.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
            class Holder extends RecyclerView.ViewHolder{

                ImageView iv;
                TextView tv;

                public Holder(View itemView) {
                    super(itemView);
                    iv= (ImageView) itemView.findViewById(R.id.iv);
                    tv= (TextView) itemView.findViewById(R.id.tv);
                }
            }
        }
    }
}
