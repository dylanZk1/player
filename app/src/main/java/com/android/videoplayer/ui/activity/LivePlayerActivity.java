package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.base.listener.OnPlayerEventListener;
import com.android.iplayer.base.media.IMediaPlayer;
import com.android.iplayer.media.core.ExoPlayerFactory;
import com.android.iplayer.media.core.IjkPlayerFactory;
import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.player.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.videoplayer.R;
import com.android.iplayer.BaseActivity;
import com.android.iplayer.base.presenter.BasePresenter;
import com.android.iplayer.widget.controls.LiveControllerControl;
import com.android.iplayer.widget.controls.DanmuWidget;
import com.android.iplayer.widget.controls.ControlLiveView;

/**
 * created by hty
 * 2022/8/25
 * Desc:这是一个直播拉流和简单的自定义直播组件交互的实例
 */
public class LivePlayerActivity extends BaseActivity {

    private DanmuWidget mDanmuWidgetView;

    private int MEDIA_CORE=1;//这里用IJkMediaPlayer作为初始解码器

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setFullScreen(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        findViewById(R.id.live_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initPlayer();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    private void initPlayer() {
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        mVideoPlayer.getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
        mVideoPlayer.setLoop(true);
        mVideoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);//设置视频画面渲染模式为：原始大小模式
        //给播放器设置一个控制器
        LiveControllerControl controller = new LiveControllerControl(mVideoPlayer.getContext());
        mVideoPlayer.setController(controller);
        //给控制器添加需要的UI交互组件
        ControlLoadingView controlLoadingView = new ControlLoadingView(controller.getContext());//加载中、开始播放按钮
        ControlStatusView controlStatusView = new ControlStatusView(controller.getContext());//播放失败、移动网络播放提示
        ControlLiveView controlLiveView = new ControlLiveView(controller.getContext());//自定义直播场景交互UI组件
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
        controller.addControllerWidget(controlLoadingView,controlStatusView,controlLiveView);
        //自定义解码器
        mVideoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
                if (1 == MEDIA_CORE) {
                    return IjkPlayerFactory.create(true).createPlayer(LivePlayerActivity.this);
                } else if (2 == MEDIA_CORE) {
                    return ExoPlayerFactory.create().createPlayer(LivePlayerActivity.this);
                } else {
                    return null;
                }
            }
        });
        mVideoPlayer.setDataSource(LIVE_RTMP2);
        mVideoPlayer.prepareAsync();//准备播放
    }
}