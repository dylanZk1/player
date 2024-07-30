package com.android.iplayer.player.controller;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.iplayer.player.R;
import com.android.iplayer.base.interfaces.IControllerView;
import com.android.iplayer.base.interfaces.IDanmuControl;
import com.android.iplayer.base.model.PlayerState;
import com.android.iplayer.base.utils.AnimationUtils;
import com.android.iplayer.base.utils.PlayerUtils;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;

/**
 * created by hty
 * 2022/6/28
 * Desc:默认的视频交互UI控制器，自定义控制器请继承{@link GestureController}或BaseController实现自己的视频UI交互控制器
 * 1、此控制器支持手势识别交互，如需自定义控制器请继承{@link GestureController}或BaseController
 * 2、此控制器只维护屏幕锁功能、点击事件传递
 * 3、如需自定义UI交互组件，请参照{@link #addControllerWidget(IControllerView)}
 */
public class VideoController extends GestureController {

    private static final int MESSAGE_CONTROL_HIDE   = 10;//延时隐藏控制器
    private static final int MESSAGE_LOCKER_HIDE    = 11;//延时隐藏屏幕锁
    private static final int DELAYED_INVISIBLE      = 5000;//延时隐藏锁时长
    private static final int MATION_DRAUTION        = 500;//控制器、控制锁等显示\隐藏过渡动画时长(毫秒)
    private View mController;//屏幕锁
    //是否播放(试看)完成\是否开启屏幕锁
    protected boolean isCompletion,isLocked;

    public VideoController(Context context) {
        super(context);
    }

    private void singleTap(MotionEvent e){
        if(isOrientationPortrait()&&isListPlayerScene()){//竖屏&&列表模式响应单击事件直接处理为开始\暂停播放事件
            if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();//回调给播放器
        }else{
            if(isLocked()){
                //屏幕锁显示、隐藏
                toggleLocker();
            }else{
                //控制器显示、隐藏
                toggleController();
            }
        }
    }

    private void doubleTap(MotionEvent e){
        if(!isLocked()){
            if (null != mVideoPlayerControl) mVideoPlayerControl.togglePlay();//回调给播放器
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_video_controller;
    }

    @Override
    public void initViews() {
        super.initViews();
        setDoubleTapTogglePlayEnabled(true);//横屏竖屏状态下都允许双击开始\暂停播放
        mController = findViewById(R.id.controller_locker);
        mController.setOnClickListener(view -> {
            stopDelayedRunnable();
            setLocker(isLocked=!isLocked);
            ((ImageView) findViewById(R.id.controller_locker_ic)).setImageResource(isLocked?R.mipmap.ic_player_locker_true:R.mipmap.ic_player_locker_false);
            Toast.makeText(getContext(),isLocked()?getString(R.string.player_locker_true):getString(R.string.player_locker_flase),Toast.LENGTH_SHORT).show();
            if(isLocked){
                hideWidget(true);//屏幕锁开启时隐藏其它所有控制器
                startDelayedRunnable(MESSAGE_LOCKER_HIDE);
            }else{
                showWidget(true);//屏幕锁关闭时显示其它所有控制器
                startDelayedRunnable(MESSAGE_CONTROL_HIDE);
            }
        });
    }

    @Override
    public void onSingleTap(MotionEvent e) {
        singleTap(e);
    }

    @Override
    public void onDoubleTap(MotionEvent e) {
        doubleTap(e);
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        super.onPlayerState(state,message);
        switch (state) {
            case STATE_RESET://初始状态\播放器还原重置
            case STATE_STOP://初始\停止
                onReset();
                break;
            case STATE_PREPARE://准备中
            case STATE_BUFFER://缓冲中
                break;
            case STATE_START://首次播放
                startDelayedRunnable(MESSAGE_CONTROL_HIDE);
                if(isOrientationLandscape()){//横屏模式下首次播放显示屏幕锁
                    if(null!=mController) mController.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_PLAY://缓冲结束恢复播放
            case STATE_ON_PLAY://生命周期\暂停情况下恢复播放
                startDelayedRunnable(MESSAGE_CONTROL_HIDE);
                break;
            case STATE_PAUSE://人为暂停中
            case STATE_ON_PAUSE://生命周期暂停中
                stopDelayedRunnable();
                break;
            case STATE_COMPLETION://播放结束
                stopDelayedRunnable();
                hideWidget(false);
                hideLockerView();
                break;
            case STATE_MOBILE://移动网络播放(如果设置允许4G播放则播放器内部不会回调此状态)
                break;
            case STATE_ERROR://播放失败
                setLocker(false);
                hideLockerView();
                break;
            case STATE_DESTROY://播放器回收
                onDestroy();
                break;
        }
    }

    /**
     * 竖屏状态下,如果用户设置返回按钮可见仅显示返回按钮,切换到横屏模式下播放时初始都不显示
     * @param orientation 更新控制器方向状态 0:竖屏 1:横屏
     */
    @Override
    public void onScreenOrientation(int orientation) {
        super.onScreenOrientation(orientation);
        if(null!= mController){
            if(isOrientationPortrait()){
                setLocker(false);
                mController.setVisibility(GONE);
            }else{
                setLocker(false);
                if(isPlayering()){
                    mController.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 显示\隐藏屏幕锁
     */
    private void toggleLocker(){
        if(null== mController) return;
        stopDelayedRunnable();
        if(mController.getVisibility()==View.VISIBLE){
            hideLockerView();
        }else{
            AnimationUtils.getInstance().startTranslateRightToLocat(mController, MATION_DRAUTION,null);
            startDelayedRunnable(MESSAGE_LOCKER_HIDE);
        }
    }

    @Override
    public void danmuControl(String tag, Object ...extra){
        if(tag.equals("SingleTap")){
            onSingleTap((MotionEvent) extra[0]);
        }else if(tag.equals("DoubleTap")){
            onDoubleTap((MotionEvent)extra[0]);
        }else if(tag.equals("Scroll")){
            if(extra.length == 4 && (extra[0] instanceof MotionEvent && extra[1] instanceof  MotionEvent && extra[2] instanceof Float && extra[3] instanceof Float))
                Scroll((MotionEvent) extra[0],(MotionEvent) extra[1],(float)extra[2],(float)extra[3]);
        }else if(tag.equals("Down")){
            if(extra.length == 1 && (extra[0] instanceof  MotionEvent))
                Down((MotionEvent) extra[0]);
        }else if(tag.equals("Touch")){
            if(extra.length == 1 && (extra[0] instanceof MotionEvent)){
                Touch((MotionEvent) extra[0],"");
            }
        }else if(tag.equals("LongClick")){
            Log.d("extra[1]",extra[1].getClass().getName());
            if(extra.length == 2 && (extra[1].getClass().isArray()) && (extra[0] instanceof IDanmakuView)){
                if(((Object[])extra[1]).length == 1 && ((Object[])extra[1])[0] instanceof MotionEvent){
                    LongClick((IDanmakuView)extra[0],(MotionEvent) ((Object[])extra[1])[0]);
                }
            }
        }
    }

    @Override
    public void danmuStart(long duration){
        for (IControllerView iControllerView : mIControllerViews) {
            if(iControllerView instanceof IDanmuControl){
                ((IDanmuControl) iControllerView).start(duration);
            }
        }
    }

    @Override
    public boolean danmuClick(IDanmakus danmakus){
        if(isLocked()){
            return false;
        }else{
            BaseDanmaku latest = danmakus.last();
            if (null != latest) {
                Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean danmuLongClick(IDanmakus danmakus){
        return false;
    }

    @Override
    public void updateTimer(DanmakuTimer timer){
        timer.update(getCurrentPosition());
    }

    /**
     * 显示\隐藏控制器
     */

    public void toggleShowController(){
        mController.setVisibility(GONE);
        //其它控制器
        hideWidget(false);
    }
    private void toggleController() {
        stopDelayedRunnable();
        if(isControllerShowing()){
            //屏幕锁
            hideLockerView();
            //其它控制器
            hideWidget(true);
        }else{
            //屏幕锁
            if(isOrientationLandscape()&&null!= mController && mController.getVisibility()!=View.VISIBLE){
                AnimationUtils.getInstance().startTranslateRightToLocat(mController, MATION_DRAUTION,null);
            }
            showWidget(true);
            startDelayedRunnable();
        }
    }

    /**
     * 结启动延时任务
     */
    @Override
    public void startDelayedRunnable() {
        startDelayedRunnable(MESSAGE_CONTROL_HIDE);
    }

    /**
     * 根据消息通道结启动延时任务
     */
    private void startDelayedRunnable(int msg) {
        super.startDelayedRunnable();
        if(null!=mExHandel){
            stopDelayedRunnable();
            Message message = mExHandel.obtainMessage();
            message.what= msg;
            mExHandel.sendMessageDelayed(message,DELAYED_INVISIBLE);
        }
    }

    /**
     * 结束延时任务
     */
    @Override
    public void stopDelayedRunnable() {
        stopDelayedRunnable(0);
    }

    /**
     * 重新开始延时任务
     */
    @Override
    public void reStartDelayedRunnable() {
        super.stopDelayedRunnable();
        stopDelayedRunnable();
        startDelayedRunnable();
    }

    /**
     * 根据消息通道取消延时任务
     * @param msg
     */
    private void stopDelayedRunnable(int msg){
        if(null!=mExHandel) {
            if(0==msg){
                mExHandel.removeCallbacksAndMessages(null);
            }else{
                mExHandel.removeMessages(msg);
            }
        }
    }

    /**
     * 使用这个Handel替代getHandel(),避免多播放器同时工作的相互影响
     */
    private ExHandel mExHandel=new ExHandel(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MESSAGE_LOCKER_HIDE ==msg.what){//屏幕锁
                hideLockerView();
            }else if(MESSAGE_CONTROL_HIDE==msg.what){//控制器
                //屏幕锁
                if(isOrientationLandscape()){
                    hideLockerView();
                }
                hideWidget(true);
            }
        }
    };

    /**
     * 隐藏屏幕锁
     */
    private void hideLockerView(){
        if(null!= mController && mController.getVisibility()==View.VISIBLE){
            AnimationUtils.getInstance().startTranslateLocatToRight(mController, MATION_DRAUTION, new AnimationUtils.OnAnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mController.setVisibility(GONE);
                }
            });
        }
    }

    /**
     * 设置给用户看的虚拟的视频总时长
     * @param totalDuration 单位：秒
     */
    public void setPreViewTotalDuration(String totalDuration) {
        int duration = PlayerUtils.getInstance().parseInt(totalDuration);
        if(duration>0) setPreViewTotalDuration(duration*1000);
    }

    /**
     * 是否启用屏幕锁功能(默认开启)，只在横屏状态下可用
     * @param showLocker true:启用 fasle:禁止
     */
    public void showLocker(boolean showLocker) {
        findViewById(R.id.controller_root).setVisibility(showLocker?View.VISIBLE:View.GONE);
    }

    /**
     * 重置内部状态
     */
    private void reset(){
        stopDelayedRunnable();
        if(null!=mExHandel) mExHandel.removeCallbacksAndMessages(null);
    }


    @Override
    public void onReset() {
        super.onReset();
        reset();
    }

    @Override
    public void onDestroy() {
        stopDelayedRunnable();
        super.onDestroy();
        reset();
    }
}