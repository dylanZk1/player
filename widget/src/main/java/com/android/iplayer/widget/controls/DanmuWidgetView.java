package com.android.iplayer.widget.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.iplayer.base.widget.BaseControlWidget;
import com.android.iplayer.base.controller.ControlWrapper;
import com.android.iplayer.base.interfaces.IDanmuControl;
import com.android.iplayer.base.media.IMediaPlayer;
import com.android.iplayer.base.model.PlayerState;
import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.widget.R;
import com.android.iplayer.widget.util.DanmuPath;
import com.android.iplayer.base.utils.ILogger;
import com.android.iplayer.widget.view.DanmuParserView;

import java.io.InputStream;

import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;


/**
 * created by lz
 * 2022/12/22
 * Desc:弹幕功能交互的控制器
 */
public abstract class DanmuWidgetView extends BaseControlWidget implements IDanmuControl {

    private DanmuParserView mDanmuPaserView;//全局的弹幕
    private DanmuParserView mZimuPaserView; //字幕view
    private InputStream danmuContent;
    private InputStream zimuContent;
    protected OriginalResource resource;

    public DanmuWidgetView(@NonNull Context context, OriginalResource resource) {
        super(context);
        this.resource = resource;
    }

    public DanmuWidgetView(@NonNull Context context, @Nullable AttributeSet attrs, OriginalResource resource) {
        super(context, attrs);
        this.resource = resource;
    }

    public DanmuWidgetView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, OriginalResource resource) {
        super(context, attrs, defStyleAttr);
        this.resource = resource;
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_controller_danmu;
    }

    @Override
    public void initViews() {
        mDanmuPaserView = findViewById(R.id.view_danmu);
        mZimuPaserView = findViewById(R.id.view_zimu);
        mDanmuPaserView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getScreenHeight()*0.6)));
        mDanmuPaserView.setController(new DanmuParserView.SetController() {
            @Override
            public void onController(String tag, Object... extra) {
                mControlWrapper.danmuControl(tag,extra);
            }

            @Override
            public boolean onDanmuClick(IDanmakus danmakus) {
                return mControlWrapper.danmuClick(danmakus);
            }

            @Override
            public boolean onDanmuLongClick(IDanmakus danmakus) {
                return mControlWrapper.danmuLongClick(danmakus);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                mControlWrapper.updateTimer(timer);
            }

            @Override
            public ControlWrapper getControlWrapper() {
                return mControlWrapper;
            }

        });
        mZimuPaserView.setController(new DanmuParserView.SetController() {
            @Override
            public void onController(String tag, Object... extra) {
                mControlWrapper.danmuControl(tag,extra);
            }

            @Override
            public boolean onDanmuClick(IDanmakus danmakus) {
                return mControlWrapper.danmuClick(danmakus);
            }

            @Override
            public boolean onDanmuLongClick(IDanmakus danmakus) {
                return mControlWrapper.danmuLongClick(danmakus);
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                mControlWrapper.updateTimer(timer);
            }

            @Override
            public ControlWrapper getControlWrapper() {
                return mControlWrapper;
            }

        });
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        ILogger.d(TAG,"onPlayerState-->state："+state+",message:"+message);
        switch (state) {
            case STATE_PREPARE://准备中
            case STATE_RESET://初始
            case STATE_STOP://停止
                onReset();
                break;
            case STATE_ERROR://播放失败
            case STATE_COMPLETION://播放结束
            case STATE_DESTROY://播放器回收
                onResetDanmu();
                break;
            case STATE_BUFFER://缓冲中
            case STATE_PAUSE://手动暂停中
            case STATE_ON_PAUSE://生命周期暂停
                onPauseDanmu();
                onPauseZimu();
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                break;
            case STATE_START://开始首帧播放
                initDanmaku();
                break;
            case STATE_PLAY://恢复播放
            case STATE_ON_PLAY://生命周期恢复播放
                onResumeDanmu();
                onResumeZimu();
                break;
        }
    }

    @Override
    public void onOrientation(int direction) {
        mDanmuPaserView.setOrientation(direction);
        mZimuPaserView.setOrientation(direction);
        if(direction == IMediaPlayer.ORIENTATION_LANDSCAPE){
            mDanmuPaserView.setDanmakuMargin(20);
            mDanmuPaserView.setTextsizeScale(1.0f);
            mZimuPaserView.setDanmakuMargin(20);
            mZimuPaserView.setTextsizeScale(1.0f);
        }else{
            mDanmuPaserView.setDanmakuMargin(10);
            mDanmuPaserView.setTextsizeScale(0.84f);
            mZimuPaserView.setDanmakuMargin(10);
            mZimuPaserView.setTextsizeScale(0.84f);
        }
    }

    @Override
    public void onPlayerScene(int playerScene) {
        findViewById(R.id.view_tool_bar).getLayoutParams().height= isOrientationPortrait()?0:getStatusBarHeight(getContext());
    }

    @Override
    public void showControl(boolean isAnimation) {

    }

    @Override
    public void start(long duration) {
        mDanmuPaserView.start(duration);
        mZimuPaserView.start(duration);
    }

    @Override
    public void onReset() {
        ILogger.d(TAG,"onReset-->");
        onResetDanmu();
    }

    @Override
    public void onDestroy() {
        ILogger.d(TAG,"onDestroy-->");
        onDestroyDanmu();
    }

    private void initDanmaku() {
        ILogger.d(TAG,"initDanmaku");
        danmuContent = DanmuPath.DanmuResource(getContext(),mDanmuPaserView, resource!=null?resource.getDanmuResource():null);
        zimuContent = DanmuPath.DanmuResource(getContext(),mDanmuPaserView, resource!=null?resource.getZimuResource():null);
        mDanmuPaserView.setDanmuContent(() -> danmuContent);
        mZimuPaserView.setDanmuContent(() -> zimuContent);
        if(null!=mDanmuPaserView) {
            mDanmuPaserView.initDanmaku();
        }
        if(null!=mZimuPaserView){
            mZimuPaserView.initDanmaku();
        }
    }

    private void onResetDanmu(){
        ILogger.d(TAG,"onResetDanmu");
        if(null!=mDanmuPaserView) mDanmuPaserView.releaseDanmaku();
        if(null!=mZimuPaserView) mZimuPaserView.releaseDanmaku();
    }

    private void onResumeDanmu(){
        ILogger.d(TAG,"onResumeDanmu");
        if(null!=mDanmuPaserView&&mDanmuPaserView.getVisibility()==VISIBLE){
            mDanmuPaserView.onResume();
        }
    }

    private void onResumeZimu(){
        ILogger.d(TAG,"onResumeZimu");
        if(null!=mZimuPaserView&&mZimuPaserView.getVisibility()==VISIBLE){
            mZimuPaserView.onResume();
        }
    }

    private void onPauseDanmu(){
        ILogger.d(TAG,"onPauseDanmu");
        if(null!=mDanmuPaserView) mDanmuPaserView.onPause();
    }

    private void onPauseZimu(){
        ILogger.d(TAG,"onPauseZimu");
        if(null!=mZimuPaserView) mZimuPaserView.onPause();
    }

    private void onDestroyDanmu(){
        ILogger.d(TAG,"onDestroyDanmu");
        if(null!=mDanmuPaserView){
            mDanmuPaserView.onDestroy();
            mDanmuPaserView=null;
        }
        if(null!=mZimuPaserView){
            mZimuPaserView.onDestroy();
            mZimuPaserView=null;
        }
    }

    public void openDanmu(){
        if(null!=mDanmuPaserView) mDanmuPaserView.setVisibility(VISIBLE);
        onResumeDanmu();
    }

    public void closeDanmu(){
        onPauseDanmu();
        if(null!=mDanmuPaserView) mDanmuPaserView.setVisibility(INVISIBLE);
    }

    public void openZimu(){
        if(null!=mZimuPaserView) mZimuPaserView.setVisibility(VISIBLE);
        onResumeZimu();
    }

    public void closeZimu(){
        onPauseZimu();
        if(null!=mZimuPaserView) mZimuPaserView.setVisibility(INVISIBLE);
    }

    /**
     * 追加弹幕数据
     * @param content 弹幕文本内容
     * @param isOneself 是否是自己发送的
     */
    public void addDanmuItem(String content,boolean isOneself){
        if(null!=mDanmuPaserView) mDanmuPaserView.addDanmuItem(content,isOneself);
    }

    public void changeDanmu(InputStream danmuContent){
        this.danmuContent = danmuContent;
    }

    public void changeZimu(InputStream zimuContent){
        this.zimuContent = zimuContent;
    }

    protected abstract int getStatusBarHeight(Context context);

    protected abstract int getScreenHeight();
}