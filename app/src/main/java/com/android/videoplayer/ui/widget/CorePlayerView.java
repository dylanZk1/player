package com.android.videoplayer.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelStoreOwner;

import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.player.controller.VideoController;
import com.android.iplayer.base.listener.OnPlayerEventListener;
import com.android.iplayer.base.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.media.core.IjkPlayerFactory;
import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.player.widget.VideoPlayer;
import com.android.iplayer.player.widget.factory.WidgetFactory;
import com.android.videoplayer.R;
import com.android.iplayer.widget.controls.DanmuWidget;

/**
 * created by hty
 * 2022/8/9
 * Desc:支持解码器交互的简单播放器封装交互组件
 */
@SuppressLint("ViewConstructor")
public class CorePlayerView extends LinearLayout {


//    private static final String URL = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";//惊奇队长
    private static final String PATH = "http://vfx.mtime.cn/Video/2019/03/18/mp4/190318231014076505.mp4";
    protected VideoPlayer mVideoPlayer;
    private int mCurrentMediaCore;//当前正在使用的解码器类型 0:系统 1:ijk 2:exo
    private String mUrl;
    private DanmuWidget mDanmuWidgetView;

    public CorePlayerView(Context context, ViewModelStoreOwner owner) {
        this(context, null, owner);
    }

    public CorePlayerView(Context context, @Nullable AttributeSet attrs, ViewModelStoreOwner owner) {
        this(context, attrs, 0, owner);
    }

    public CorePlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, ViewModelStoreOwner owner) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_player_core, this);
        mVideoPlayer = findViewById(R.id.view_video_player);
        findViewById(R.id.view_player_container).getLayoutParams().height = getResources().getDisplayMetrics().widthPixels * 9 / 16;//给播放器固定一个高度
        VideoController controller = mVideoPlayer.createController();
        mDanmuWidgetView = new DanmuWidget(controller.getContext(), new OriginalResource() {
            @Override
            public Object getDanmuResource() {
                return "397811815.xml";
            }

            @Override
            public Object getZimuResource() {
                return null;
            }
        });
        controller.addControllerWidget(mDanmuWidgetView,0);
        WidgetFactory.bindDefaultControls(controller,owner);//不显示返回按钮\不添加悬浮窗窗口交互UI组件
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                if (1 == mCurrentMediaCore) {
                    return IjkPlayerFactory.create().createPlayer(getContext());//IJK解码器
                } else if (2 == mCurrentMediaCore) {
                    return ExoPlayerFactory.create().createPlayer(getContext());//EXO解码器
                } else {
                    return null;//播放器内部默认的
                }
            }
        });
        OnClickListener onClickListener = view -> {
            switch (view.getId()) {
                case R.id.view_core_1:
                    if(0!=mCurrentMediaCore){
                        findViewById(R.id.view_core_1).setSelected(true);
                        findViewById(R.id.view_core_2).setSelected(false);
                        findViewById(R.id.view_core_3).setSelected(false);
                        mCurrentMediaCore = 0;
                        rePlay();
                    }
                    break;
                case R.id.view_core_2:
                    if(1!=mCurrentMediaCore){
                        findViewById(R.id.view_core_1).setSelected(false);
                        findViewById(R.id.view_core_2).setSelected(true);
                        findViewById(R.id.view_core_3).setSelected(false);
                        mCurrentMediaCore = 1;
                        rePlay();
                    }
                    break;
                case R.id.view_core_3:
                    if(2!=mCurrentMediaCore){
                        findViewById(R.id.view_core_1).setSelected(false);
                        findViewById(R.id.view_core_2).setSelected(false);
                        findViewById(R.id.view_core_3).setSelected(true);
                        mCurrentMediaCore = 2;
                        rePlay();
                    }
                    break;
            }

        };
        mCurrentMediaCore=0;
        findViewById(R.id.view_core_1).setSelected(true);
        findViewById(R.id.view_core_1).setOnClickListener(onClickListener);
        findViewById(R.id.view_core_2).setOnClickListener(onClickListener);
        findViewById(R.id.view_core_3).setOnClickListener(onClickListener);
    }

    /**
     * 重新播放
     */
    private void rePlay() {
        if (null != mVideoPlayer) {
            mVideoPlayer.onReset();
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(TextUtils.isEmpty(mUrl)? PATH :mUrl);//播放地址设置
            mVideoPlayer.prepareAsync();//开始异步准备播放
        }
    }

    public void onResume() {
        if (null != mVideoPlayer) mVideoPlayer.onResume();
    }

    public void onPause() {
        if (null != mVideoPlayer) mVideoPlayer.onPause();
    }

    public boolean isBackPressed() {
        if (null != mVideoPlayer) {
            if (mVideoPlayer.isBackPressed()) {
                return true;
            }
            return false;
        }
        return true;
    }

    public void onDestroy() {
        if (null != mVideoPlayer) mVideoPlayer.onDestroy();
    }

    public void start(String url) {
        if (null != mVideoPlayer) {
            this.mUrl=url;
            mVideoPlayer.setLoop(false);
            mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_CROPPING);
            mVideoPlayer.setProgressCallBackSpaceMilliss(300);
            mVideoPlayer.getController().setTitle("测试播放地址");//视频标题(默认视图控制器横屏可见)
            mVideoPlayer.setDataSource(TextUtils.isEmpty(url)? PATH :url);//播放地址设置
            mVideoPlayer.prepareAsync();//开始异步准备播放
        }
    }
}