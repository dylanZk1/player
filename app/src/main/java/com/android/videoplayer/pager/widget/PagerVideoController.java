package com.android.videoplayer.pager.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.iplayer.widget.util.ScreenUtils;
import com.android.videoplayer.App;
import com.android.videoplayer.R;
import com.android.videoplayer.pager.base.BaseViewPager;
import com.android.videoplayer.pager.bean.VideoBean;
import com.android.videoplayer.utils.GlideModel;
import com.android.iplayer.base.utils.Logger;

/**
 * created by hty
 * 2022/7/1
 * Desc:Pager片段播放器UI交互
 */
public class PagerVideoController extends BaseViewPager {

    private VideoBean mMediaInfo;
    private FrameLayout mPlayerContainer;
    private MusicDiscView mDiscView;

    public PagerVideoController(@NonNull Context context) {
        super(context);
    }

    public PagerVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PagerVideoController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.player_control_functionbar_portral_local;
    }

    @Override
    protected void initViews() {
        Logger.d(TAG,"initViews-->"+getPositionStr());
    }

    /**
     * 初始化绑定媒体信息
     * @param videoInfo 多媒体基本信息
     * @param position 位置
     */
    public void initMediaData(VideoBean videoInfo, int position) {
        super.mPosition =position;
        this.mMediaInfo=videoInfo;
        if(null!=mMediaInfo){
            TextView view_tv_author = findViewById(R.id.view_tv_author);
            TextView view_tv_describe = findViewById(R.id.view_tv_describe);
            TextView view_music_name = findViewById(R.id.view_music_name);
            TextView view_tv_like = findViewById(R.id.view_tv_like);
            TextView view_tv_comment = findViewById(R.id.view_tv_comment);
            TextView view_tv_share = findViewById(R.id.view_tv_share);
            ImageView view_ic_avatar = findViewById(R.id.view_ic_avatar);
            view_tv_author.setText(String.format("@%s",videoInfo.getAuthorName()));
            view_tv_describe.setText(videoInfo.getTitle());
            view_music_name.setText(videoInfo.getMusicName());
            view_tv_like.setText(ScreenUtils.getInstance(App.getInstance().getContext()).formatWan(videoInfo.getLikeCount(),true));
            view_tv_comment.setText(ScreenUtils.getInstance(App.getInstance().getContext()).formatWan(videoInfo.getPlayCount(),true));
            view_tv_share.setText(videoInfo.getFormatPlayCountStr());
            ImageView imageView = findViewById(R.id.pager_cover);
            GlideModel.getInstance().loadCirImage(view_ic_avatar,mMediaInfo.getAuthorImgUrl());
            GlideModel.getInstance().loadImage(imageView,mMediaInfo.getCoverImgUrl());
        }
        //唱片机初始化
        mDiscView = findViewById(R.id.view_dic_cover);
        assert videoInfo != null;
        mDiscView.setMusicFront(videoInfo.getMusicImgUrl());
    }

    /**
     * 返回播放器容器
     * @return 播放器容器
     */
    public ViewGroup getPlayerContainer(){
        if(null==mPlayerContainer){
            mPlayerContainer = findViewById(R.id.pager_player_container);
        }
        return mPlayerContainer;
    }

    public VideoBean getVideoData() {
        return mMediaInfo;
    }

    @Override
    public void prepare() {
        Logger.d(TAG,"prepare-->"+getPositionStr());
        if(null!=mDiscView) mDiscView.onResume();
    }

    @Override
    public void resume() {
        Logger.d(TAG,"resume-->"+getPositionStr());
        if(null!=mDiscView) mDiscView.onResume();
    }

    @Override
    public void pause() {
        Logger.d(TAG,"pause-->"+getPositionStr());
        if(null!=mDiscView) mDiscView.onPause();
    }

    @Override
    public void stop() {
        Logger.d(TAG,"stop-->"+getPositionStr());
        if(null!=mDiscView) mDiscView.onStop();
    }

    @Override
    public void error() {
        Logger.d(TAG,"error-->"+getPositionStr());
    }

    @Override
    public void onRelease() {
        Logger.d(TAG,"onRelease-->"+getPositionStr());
        if(null!=mDiscView) mDiscView.onRelease();
        ViewGroup playerContainer = getPlayerContainer();
        if(null!=playerContainer){
            playerContainer.removeAllViews();
        }
    }
}