package com.allen.mm.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.allen.mm.app.*;
import com.allen.mm.app.model.Model;
import com.allen.mm.app.utils.CacheManager;
import com.allen.mm.app.utils.Utils;
import com.baoyz.widget.PullRefreshLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/12
 */
public class MMFragment extends BaseFragment implements PullRefreshLayout.OnRefreshListener{
    public static final String CACHE="CACHE";
    DisplayImageOptions displayImageOptions;
    PullRefreshLayout refreshLayout;
    RecyclerView recyclerView;

    int page=0;
    int pageSize=30;
    AsyncHttpClient httpClient;

    ArrayList<Model> list;

    public static final String TAB="tab";
    public static final int TAB_MM=0;
    public static final int TAB_XG=1;
    public static final int TAB_SW=2;
    private int currentTab=0;


    public static MMFragment instance(int tab){
        MMFragment fragment=new MMFragment();
        Bundle args=new Bundle();
        args.putInt(TAB,tab);
        fragment.setArguments(args);
        return fragment;
    }

    public MMFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpClient=new AsyncHttpClient();
        list=new ArrayList<Model>();
        currentTab=getArguments().getInt(TAB);
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
                .imageScaleType(ImageScaleType.NONE)
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
        refreshLayout= (PullRefreshLayout) rootView.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
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
                int into[]=new int[2];
                staggeredGridLayoutManager.findLastVisibleItemPositions(into);
                if (!refreshLayout.isRefreshing()
                        && recyclerView.getAdapter().getItemCount() == Math.max(into[0],into[1])+1
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    page+=30;
                    getData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //解决RecyclerView和SwipeRefreshLayout共用存在的bug
//                int into[]=new int[2];
//                staggeredGridLayoutManager
//                        .findFirstCompletelyVisibleItemPositions(into);
//                refreshLayout.setEnabled(into[0] == 0||into[1]==0);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            ArrayList tempList=savedInstanceState.getParcelableArrayList("list");
            if(tempList!=null){
                this.list.clear();
                this.list.addAll(tempList);
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
            //取本地：
            CacheManager.getInstance().getAsync(getActivity(),CACHE+currentTab, new CacheManager.CacheHandler<ArrayList>() {

                @Override
                public void onSuccess(ArrayList arrayList) {
                    if(arrayList==null){
                        Toast.makeText(getActivity(), "加载失败,请检查网络", Toast.LENGTH_LONG).show();
                        return;
                    }
                    list.clear();
                    list.addAll(arrayList);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "CacheManager.getAsync errer");
                    Toast.makeText(getActivity(),"加载失败,请检查网络",Toast.LENGTH_LONG).show();
                }
            });
//                Toast.makeText(getActivity(),"加载失败,请检查网络",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //缓存到本地
        CacheManager.getInstance().putAsync(getActivity(),CACHE+currentTab, list, new CacheManager.CacheHandler<ArrayList>() {
            @Override
            public void onSuccess(ArrayList arrayList) {
                Log.d("MainActivity", "CacheManager ok");
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                Log.e("MainActivity", "CacheManager.putAsync errer");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CacheManager.getInstance().destory();
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
        refreshLayout.setRefreshing(true);
        getData();
    }
    private void getData(){
        String url=Config.API_MM;
        if(currentTab==TAB_MM)
           url=Config.API_MM;
        else if(currentTab==TAB_SW)
            url=Config.API_MM_SW;
        else if(currentTab==TAB_XG)
            url=Config.API_MM_XG;
        httpClient.get(getActivity(), String.format(url, page, pageSize),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                refreshLayout.setRefreshing(false);
                if(response!=null){
                    if(currentTab==0)
                        parseJson(response);
                    else
                        try {
                            parseJson(response.getJSONObject("searchResult"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                refreshLayout.setRefreshing(false);
                Log.e("MainActivity",throwable.getMessage());
                Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseJson(JSONObject response){
        int preLength=list.size();
        try {
            JSONArray imags=response.getJSONArray("data");
            if(imags!=null){
                if(page==0){
                    list.clear();
                    preLength=0;
                }
                for (int i = 0; i < imags.length(); i++) {
                    JSONObject object=imags.getJSONObject(i);
                    Model model=new Model();
                    if(currentTab==0){
                        model.desc=object.optString("desc");
                        model.thumbnail_url=object.getString("thumbnail_url");
                        model.thumb_large_url=object.getString("thumb_large_url");
                        model.image_url=object.getString("image_url");
                        model.thumbnail_height=object.getInt("thumbnail_height");
                        model.thumbnail_width=object.getInt("thumbnail_width");
                        model.thumb_large_height=object.getInt("thumb_large_height");
                        model.thumb_large_width=object.getInt("thumb_large_width");
                    }else{
                        model.desc=object.optString("di");
                        model.thumbnail_url=object.getString("mid_url");
                        model.thumb_large_url=object.getString("obj_url");
                        model.image_url=object.getString("obj_url");
                        model.thumbnail_height=object.getInt("height")/3;
                        model.thumbnail_width=object.getInt("width")/3;
                        model.thumb_large_height=object.getInt("height");
                        model.thumb_large_width=object.getInt("width");
                    }
                    list.add(model);
                    if(preLength==0)
                        recyclerView.getAdapter().notifyDataSetChanged();
                    else
                        recyclerView.getAdapter().notifyItemInserted(preLength-1+i);
                }
//                            recyclerView.getAdapter().notifyItemRangeInserted(preLength,imags.length());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder>{

        @Override
        public ImageAdapter.Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(getActivity()).inflate(R.layout.item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(final ImageAdapter.Holder viewHolder, int i) {
            final Model model=list.get(i);
            viewHolder.tv.setText(model.desc);
            String imagUrl=density>=2?model.thumb_large_url:model.thumbnail_url;
            RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) viewHolder.iv.getLayoutParams();
            int w=width-Utils.dip2px(getActivity(),20);
            params.height=density>=2?(w/2*model.thumb_large_height/model.thumb_large_width):(w/2*model.thumbnail_height/model.thumbnail_width);
            viewHolder.iv.setLayoutParams(params);
            ImageLoader.getInstance().displayImage(imagUrl,viewHolder.iv,displayImageOptions);
//                ImageLoader.getInstance().loadImage(imagUrl,displayImageOptions, new ImageLoadingListener() {
//                    @Override
//                    public void onLoadingStarted(String s, View view) {
//
//                    }
//
//                    @Override
//                    public void onLoadingFailed(String s, View view, FailReason failReason) {
//                        viewHolder.iv.setImageResource(R.drawable.empty_photo);
//                    }
//
//                    @Override
//                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) viewHolder.iv.getLayoutParams();
//                        params.height=bitmap.getHeight();
////                        Log.e("MainActivity","height="+params.height);
//                        viewHolder.iv.setLayoutParams(params);
//                        viewHolder.iv.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onLoadingCancelled(String s, View view) {
//
//                    }
//                });

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    int x = location[0];
                    int y = location[1];
                    Intent intent=new Intent(getActivity(),ImageActivity.class);
                    intent.putExtra("model",(Parcelable)model);
                    ActivityOptionsCompat optionsCompat=ActivityOptionsCompat.makeScaleUpAnimation(v, x/2,y/2,v.getWidth(),v.getHeight());
                    ActivityCompat.startActivity(getActivity(), intent, optionsCompat.toBundle());
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
