package com.allen.mm.app.fragment;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.allen.mm.app.*;
import com.allen.mm.app.model.VideoModel;
import com.allen.mm.app.utils.Utils;
import com.baoyz.widget.PullRefreshLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 描述:
 *
 * @author: liyong on 2015/1/14
 */
public class VideoFragment extends BaseFragment implements PullRefreshLayout.OnRefreshListener {
    DisplayImageOptions displayImageOptions;
    AsyncHttpClient httpClient;
    PullRefreshLayout pullRefreshLayout;
    RecyclerView recyclerView;
    VideoAdapter videoAdapter;
    int pageSize=20;
    String maxTime;
    ArrayList<VideoModel> list;


    public static VideoFragment instance(){
        return new VideoFragment();
    }
    public VideoFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpClient=new AsyncHttpClient();
        list=new ArrayList<VideoModel>();
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
        View rootView=inflater.inflate(R.layout.giffragment_layout,null);
        pullRefreshLayout= (PullRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        recyclerView= (RecyclerView) rootView.findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        pullRefreshLayout.setOnRefreshListener(this);
        pullRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
        videoAdapter=new VideoAdapter();
        recyclerView.setAdapter(videoAdapter);
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
                if (!pullRefreshLayout.isRefreshing()
                        && recyclerView.getAdapter().getItemCount() ==linearLayoutManager.findLastVisibleItemPosition()+1
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    getData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (videoAdapter.mHolder != null) {
                  //  Log.d("ly","videoAdapter.mHolder!=null,+findFirstVisibleItemPosition="+linearLayoutManager.findFirstVisibleItemPosition()+",videoAdapter.mHolder.position="+videoAdapter.mHolder.position);
                    if (linearLayoutManager.findFirstVisibleItemPosition() > videoAdapter.mHolder.position || linearLayoutManager.findLastVisibleItemPosition() < videoAdapter.mHolder.position) {
                        Log.d("ly","onScrolled--"+videoAdapter.mHolder.position);
                        videoAdapter.mHolder.videoView.stopPlayback();
                        videoAdapter.mHolder.layout_bottom.setVisibility(View.VISIBLE);
                        videoAdapter.mHolder.iv_player.setVisibility(View.VISIBLE);
                        videoAdapter.mHolder.iv_video.setVisibility(View.VISIBLE);
                        videoAdapter.mHolder=null;
                    }
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoAdapter.mHolder != null) {
            //  Log.d("ly","videoAdapter.mHolder!=null,+findFirstVisibleItemPosition="+linearLayoutManager.findFirstVisibleItemPosition()+",videoAdapter.mHolder.position="+videoAdapter.mHolder.position);
            Log.d("ly","onScrolled--"+videoAdapter.mHolder.position);
            videoAdapter.mHolder.videoView.stopPlayback();
            videoAdapter.mHolder.layout_bottom.setVisibility(View.VISIBLE);
            videoAdapter.mHolder.iv_player.setVisibility(View.VISIBLE);
            videoAdapter.mHolder.iv_video.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null)
            outState.putParcelableArrayList("list",list);
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
                onRefresh();
            }
        }else
            onRefresh();
    }

    @Override
    public void onRefresh() {
        pullRefreshLayout.setRefreshing(true);
        maxTime="";
        getData();
    }
    private void getData(){
//        Log.d("ly","getData--maxTime="+maxTime);
        httpClient.get(getActivity(),String.format(Config.API_SP, pageSize, maxTime),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                pullRefreshLayout.setRefreshing(false);
                int preLength=list.size();
                if(response!=null){
                    try {
                        JSONObject info=response.getJSONObject("info");
                        maxTime=info.getString("maxtime");
                        JSONArray imags=response.getJSONArray("list");
                        if(imags!=null){
                            if(TextUtils.isEmpty(maxTime)){
                                list.clear();
                                preLength=0;
                            }
                            for (int i = 0; i < imags.length(); i++) {
                                JSONObject object=imags.getJSONObject(i);
                                VideoModel model=new VideoModel();
                                model.text=object.optString("text");
                                model.playcount=object.getLong("playcount");
                                model.height=object.getInt("height");
                                model.width=object.getInt("width");
                                model.image0=object.getString("image0");
                                model.image1=object.getString("image1");
                                model.image2=object.getString("image2");
                                model.videotime=object.getInt("videotime");
                                model.videouri=object.getString("videouri");
                                list.add(model);
                                if(preLength==0)
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
                pullRefreshLayout.setRefreshing(false);
                Log.e("VideoActivity", throwable.getMessage());
                Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getTime(int second){
        if(second<10)
            return "00:0"+second;
        else if(second<60)
            return "00:"+second;
        else{
            int m=second/60;
            int s=second%60;
            return  (m>10?(""+m):("0"+m))+":"+(s>10?""+s:"0"+s);
        }
    }

    class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Holder>{

        VideoAdapter.Holder mHolder;

        @Override
        public VideoAdapter.Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new Holder(LayoutInflater.from(getActivity()).inflate(R.layout.video_item,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(final VideoAdapter.Holder viewHolder,final int i) {
            final VideoModel model=list.get(i);
            viewHolder.tv.setText(model.text);
            viewHolder.tv_count.setText(model.playcount+"");
            viewHolder.tv_time.setText(getTime(model.videotime));

            LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) viewHolder.layout_video.getLayoutParams();
            int w=width- Utils.dip2px(getActivity(), 30);
            params.height=w*model.height/model.width;
            viewHolder.layout_video.setLayoutParams(params);

            viewHolder.layout_bottom.setVisibility(View.VISIBLE);
            viewHolder.iv_player.setVisibility(View.VISIBLE);
            viewHolder.iv_video.setVisibility(View.VISIBLE);

            String  imagUrl= TextUtils.isEmpty(model.image0)?model.image1:model.image2;
            ImageLoader.getInstance().displayImage(imagUrl,viewHolder.iv_video,displayImageOptions);

            viewHolder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
//                    viewHolder.videoView.pause();
                }
            });
            viewHolder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    viewHolder.videoView.pause();
                    viewHolder.layout_bottom.setVisibility(View.VISIBLE);
                    viewHolder.iv_player.setVisibility(View.VISIBLE);
                    viewHolder.iv_video.setVisibility(View.VISIBLE);
                }
            });
            viewHolder.position=i;
            viewHolder.videoView.setVideoURI(Uri.parse(model.videouri));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mHolder==viewHolder){//点击是当前的
                        //播放视频
                        if(viewHolder.videoView.isPlaying()) {
                            viewHolder.videoView.pause();
                            viewHolder.layout_bottom.setVisibility(View.VISIBLE);
                            viewHolder.iv_player.setVisibility(View.VISIBLE);
                        }
                        else{
                            viewHolder.layout_bottom.setVisibility(View.GONE);
                            viewHolder.iv_player.setVisibility(View.GONE);
                            viewHolder.iv_video.setVisibility(View.GONE);
                            viewHolder.videoView.start();
                        }
                    }else{
                        if(mHolder!=null&&mHolder.videoView.isPlaying()){//其他的正在播放
                            mHolder.videoView.pause();
                            mHolder.layout_bottom.setVisibility(View.VISIBLE);
                            mHolder.iv_player.setVisibility(View.VISIBLE);
                            mHolder.iv_video.setVisibility(View.VISIBLE);
                        }
                        //播放视频
                        if(viewHolder.videoView.isPlaying()) {
                            viewHolder.videoView.pause();
                            viewHolder.layout_bottom.setVisibility(View.VISIBLE);
                            viewHolder.iv_player.setVisibility(View.VISIBLE);
                        }
                        else{
                            mHolder=viewHolder;
                            viewHolder.layout_bottom.setVisibility(View.GONE);
                            viewHolder.iv_player.setVisibility(View.GONE);
                            viewHolder.iv_video.setVisibility(View.GONE);
                            viewHolder.videoView.start();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
        class Holder extends RecyclerView.ViewHolder{

            int position;
            VideoView videoView;
            TextView tv;
            ImageView iv_video;
            ImageView iv_player;
            View layout_bottom;
            RelativeLayout layout_video;
            TextView tv_count;
            TextView tv_time;

            public Holder(View itemView) {
                super(itemView);
                videoView= (VideoView) itemView.findViewById(R.id.videoView);
                tv= (TextView) itemView.findViewById(R.id.tv_title);
                iv_video= (ImageView) itemView.findViewById(R.id.iv_video);
                iv_player= (ImageView) itemView.findViewById(R.id.iv_play);
                layout_bottom=itemView.findViewById(R.id.layout_bottom);
                layout_video= (RelativeLayout) itemView.findViewById(R.id.layout_video);
                tv_count= (TextView) itemView.findViewById(R.id.tv_count);
                tv_time= (TextView) itemView.findViewById(R.id.tv_time);
            }
        }
    }
}
