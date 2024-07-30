package com.android.videoplayer.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.android.iplayer.player.controller.VideoController;
import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.player.widget.VideoPlayer;
import com.android.iplayer.player.widget.factory.WidgetFactory;
import com.android.videoplayer.R;
import com.android.iplayer.BaseActivity;
import com.android.iplayer.base.presenter.BasePresenter;
import com.android.iplayer.widget.controls.DanmuWidget;
import com.android.videoplayer.ui.widget.TitleView;

/**
 * created by hty
 * 2022/6/22
 * Desc:这是一个支持带弹幕控制的常规视频播放器控件封装的示例
 */
public class DanmuPlayerActivity extends BaseActivity {

    private DanmuWidget mDanmuWidgetView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        TitleView titleView = (TitleView) findViewById(R.id.title_view);
        titleView.setTitle(getIntent().getStringExtra("title"));
        titleView.setOnTitleActionListener(this::onBackPressed);
        initPlayer();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    /**
     * 播放器初始化及调用示例
     */
    private void initPlayer() {
        initSetting();
        //播放器播放之前准备工作
        mVideoPlayer = (VideoPlayer) findViewById(R.id.video_player);
        findViewById(R.id.player_container).getLayoutParams().height= getResources().getDisplayMetrics().widthPixels * 9 /16;//给播放器固定一个高度
        //给播放器设置一个控制器
        VideoController controller = mVideoPlayer.createController();
        WidgetFactory.bindDefaultControls(controller,this);
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
        //将弹幕组件添加到控制器最底层
        controller.addControllerWidget(mDanmuWidgetView,0);
//        mDanmuWidgetView.setDanmuData(DataFactory.getInstance().getDanmus());//添加弹幕数据
        mVideoPlayer.getController().setTitle("弹幕视频测试播放地址");//视频标题(默认视图控制器横屏可见)
        mVideoPlayer.setDataSource(MP4_URL1);//播放地址设置
        mVideoPlayer.prepareAsync();//开始异步准备播放
    }

    private void initSetting() {
        //如果是弹幕播放场景则添加弹幕组件
        Switch aSwitch = (Switch) findViewById(R.id.switch_danmu);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(null!= mDanmuWidgetView){
                if(isChecked){
                    mDanmuWidgetView.openDanmu();
                    ((TextView) findViewById(R.id.tv_danmu)).setText("关闭弹幕");
                }else{
                    mDanmuWidgetView.closeDanmu();
                    ((TextView) findViewById(R.id.tv_danmu)).setText("开启弹幕");
                }
            }
        });
        findViewById(R.id.danmu_content).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_send_danmu).setOnClickListener(v -> {
            if(null!= mDanmuWidgetView){
                mDanmuWidgetView.addDanmuItem("这是我发的有颜色的弹幕！",true);
            }
        });
    }
}