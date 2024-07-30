package com.android.iplayer;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.iplayer.base.AbstractMediaPlayer;
import com.android.iplayer.player.base.BasePlayer;
import com.android.iplayer.player.controller.VideoController;
import com.android.iplayer.base.interfaces.IRenderView;
import com.android.iplayer.base.listener.OnPlayerEventListener;
import com.android.iplayer.player.manager.IWindowManager;
import com.android.iplayer.base.media.IMediaPlayer;
import com.android.iplayer.media.core.IjkPlayerFactory;
import com.android.iplayer.base.utils.ContentToString;
import com.android.iplayer.base.model.PlayerState;
import com.android.iplayer.player.widget.factory.WidgetFactory;
import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.player.widget.VideoPlayer;
import com.android.iplayer.widget.controls.ControlCompletionView;
import com.android.iplayer.widget.controls.ControlFunctionBarView;
import com.android.iplayer.widget.controls.ControlGestureView;
import com.android.iplayer.widget.controls.ControlLoadingView;
import com.android.iplayer.widget.controls.ControlStatusView;
import com.android.iplayer.widget.controls.ControlToolBarView;
import com.android.iplayer.base.presenter.BasePresenter;
import com.android.iplayer.widget.controls.DanmuWidget;
import com.android.iplayer.base.utils.Logger;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LocalVideo extends BaseActivity {

    private VideoPlayer videoPlayer;
    private DanmuWidget mDanmuWidgetView;

    private boolean is_global;

    private pipListener movieViewModel;

    private SoftReference<Context> contextLocal;

    private static final String ACTION_MEDIA_CONTROL = "media_control";//intent的事件
    private static final String EXTRA_CONTROL_TYPE = "control_type";//事件类型

    private static final int CONTROL_TYPE_PLAY = 1;
    private static final int CONTROL_TYPE_PAUSE = 2;
    private static final int CONTROL_TYPE_REPLAY = 3;
    private static final int REQUEST_PLAY = 1;//播放事件
    private static final int REQUEST_PAUSE = 2;
    private static final int REQUEST_REPLAY = 3;

    protected boolean isSupportPipMode = false;
    //是否已经在画中画模式(自行判断赋值时机)
    public boolean isInPIPMode = false;
    //是否点击进入过画中画模式--用于判断程序在后台时,由画中画返回全屏后退出,是否启动首页activity,以及onstop配合判断是否点击进入过画中画且在画中画模式
    public boolean isEnteredPIPMode = false;

    private BroadcastReceiver mReceiver =new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                return;
            }
            final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
            Logger.d(TAG,"controlType:"+controlType);
            switch (controlType) {
                case CONTROL_TYPE_PLAY:
                case CONTROL_TYPE_PAUSE:
                case CONTROL_TYPE_REPLAY:
                    if(null!=mVideoPlayer) mVideoPlayer.togglePlay();
                    break;
            }
        }
    };

    private final PictureInPictureParams.Builder mBuilder = new PictureInPictureParams.Builder();
    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getData() != null){
            Log.d("videoURI",getIntent().getData().toString());
        }
        movieViewModel = new ViewModelProvider(this).get(pipListener.class);
        movieViewModel.getEnterOrExitPiPMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEntering) {
                if (isEntering != null) {
                    if (isEntering) {
                        // 进入画中画模式的逻辑
                    } else {
//                        finish();
                    }
                }
            }
        });
        movieViewModel.setEnterOrExitPiPMode(true);
        resetReceiver();
        is_global = "1".equals(getIntent().getStringExtra("is_global"));
        contextLocal = new SoftReference<>(this);
        forbidCycle();
        startFullScreen(is_global, getIntent().getData() != null? getIntent().getData().toString(): "");
    }

    private String getMimeType(String url) {
        String extension = url.substring(url.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
        return mimeType;
    }

    private Object getOriDanmuResource(String datasource){
        Uri aa = Uri.parse(datasource);
        if(TextUtils.isEmpty(aa.getScheme())){
            return null;
        }else if(aa.getScheme().equals("content")){
            String path = ContentToString.getAbsolutePath(LocalVideo.this, Uri.parse(datasource));
            if(!TextUtils.isEmpty(path)){
                String extension = path.substring(path.lastIndexOf("."));
                String danmuPath = path.replace(extension,".xml");
                if(new File(danmuPath).exists()){
                    return new File(danmuPath);
                }
                return null;
            }

            return null;
        }else{
            return null;
        }

    }

    /**
     * 任意界面创建一个全屏窗口播放器并开始播放
     */
    private void startFullScreen(boolean isGlobal, String dataSource) {
        BasePlayer basePlayer = IWindowManager.getInstance().getBasePlayer();
        if(isGlobal&&null!=basePlayer){
            IWindowManager.getInstance().onClean();
            mVideoPlayer= (VideoPlayer) basePlayer;
            mVideoPlayer.setParentContext(this);
            mVideoPlayer.setLandscapeWindowTranslucent(true);//全屏沉浸样式
            mVideoPlayer.hideSystemBarStatus(this);
//            mVideoPlayer.startFullScreen();//开启全屏播放
            FrameLayout rootLayout = new FrameLayout(this);
            rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            rootLayout.addView(mVideoPlayer);
            setContentView(rootLayout);

            return;
        }
//        IWindowManager.getInstance().quitGlobaWindow();
        videoPlayer = new VideoPlayer(this);
        videoPlayer.setParentContext(this);
        videoPlayer.setIsOutLink(false,true);
        videoPlayer.setBackgroundColor(Color.parseColor("#000000"));
        videoPlayer.setZoomModel(IMediaPlayer.MODE_ZOOM_TO_FIT);
        VideoController controller=new VideoController(videoPlayer.getContext());
        videoPlayer.setController(controller);//给播放器设置控制器
        //给播放器控制器绑定需要的自定义UI交互组件
        ControlToolBarView toolBarView=new ControlToolBarView(this);//标题栏，返回按钮、视频标题、功能按钮、系统时间、电池电量等组件
        toolBarView.showMenus(false,true,true);
        toolBarView.setOnToolBarActionListener(new ControlToolBarView.OnToolBarActionListener() {
            @Override
            public void onBack() {//仅当设置showBack(true)后并且竖屏情况下才会有回调到此
                Logger.d(TAG,"onBack");
                onBackPressed();
            }

            @Override
            public void onTv() {
                Logger.d(TAG,"onTv");
            }

            @Override
            public void onWindow() {
                Logger.d(TAG,"onWindow");
                controller.toggleShowController();
                clearDialog(mMenuDialog);
                enterPicture(null);
            }

            @Override
            public void onMenu() {
                Logger.d(TAG,"onMenu");
                showMenuDialog();
            }
        });
        ControlFunctionBarView functionBarView=new ControlFunctionBarView(this,this);//底部时间、seek、静音、全屏功能栏
        functionBarView.showSoundMute(true,false);//启用静音功能交互\默认不静音
        ControlStatusView statusView=new ControlStatusView(this);//移动网络播放提示、播放失败、试看完成
        ControlGestureView gestureView=new ControlGestureView(this);//手势控制屏幕亮度、系统音量、快进、快退UI交互
        ControlCompletionView completionView=new ControlCompletionView(this);//播放完成、重试
        ControlLoadingView loadingView=new ControlLoadingView(this);//加载中、开始播放
        mDanmuWidgetView = new DanmuWidget(controller.getContext(), new OriginalResource() {
            @Override
            public Object getDanmuResource() {
                return getOriDanmuResource(dataSource);
            }

            @Override
            public Object getZimuResource() {
                return null;
            }
        });
        controller.addControllerWidget(mDanmuWidgetView,0);

        controller.addControllerWidget(toolBarView,functionBarView,statusView,gestureView,completionView,loadingView);
        //如果适用自定义解码器则必须实现setOnPlayerActionListener并返回一个多媒体解码器
        videoPlayer.setOnPlayerActionListener(new OnPlayerEventListener() {
            /**
             * 创建一个自定义的播放器,返回null,则内部自动创建一个默认的解码器
             * @return
             */
            @Override
            public AbstractMediaPlayer createMediaPlayer() {
//                if(ContentToString.getFileNameWithSuffix(ContentToString.getAbsolutePath(LocalVideo.this, Uri.parse(dataSource))).endsWith(".avi")
//                        || ContentToString.getFileNameWithSuffix(ContentToString.getAbsolutePath(LocalVideo.this, Uri.parse(dataSource))).endsWith(".rmvb")
//                        || ContentToString.getFileNameWithSuffix(ContentToString.getAbsolutePath(LocalVideo.this, Uri.parse(dataSource))).endsWith(".mpg") ){
//                    return IjkPlayerFactory.create().createPlayer(LocalVideo.this);
//                }
//                return ExoPlayerFactory.create().createPlayer(LocalVideo.this);
                return IjkPlayerFactory.create().createPlayer(LocalVideo.this);
            }

            @Override
            public void onPlayerState(PlayerState state, String message) {
                Logger.d(TAG,"onPlayerState-->state:"+state+",message:"+message);
                switch (state) {
                    case STATE_PREPARE://播放器准备中
                    case STATE_BUFFER://播放过程缓冲中
                        break;
                    case STATE_START://缓冲结束、准备结束 后的开始播放
                    case STATE_PLAY://恢复播放
                    case STATE_ON_PLAY:
                        updatePictureInPictureActions(R.mipmap.ic_player_pause, "播放", CONTROL_TYPE_PLAY, REQUEST_PLAY);
                        break;
                    case STATE_MOBILE://移动网络环境下播放
                        break;
                    case STATE_ON_PAUSE: //手动暂停生命周期暂停
                    case STATE_PAUSE://手动暂停
                        updatePictureInPictureActions(R.mipmap.ic_player_play, "暂停", CONTROL_TYPE_PAUSE, REQUEST_PAUSE);
                        break;
                    case STATE_RESET: //重置
                    case STATE_STOP://停止
                    case STATE_DESTROY://销毁
                    case STATE_COMPLETION://正常的播放器结束
                    case STATE_ERROR://失败
                        updatePictureInPictureActions(R.mipmap.ic_player_window_replay, "重新播放", CONTROL_TYPE_REPLAY, REQUEST_REPLAY);
                        break;
                }
            }

            @Override
            public IRenderView createRenderView() {
                return null;
            }

        });
        videoPlayer.setLandscapeWindowTranslucent(true);//全屏沉浸样式
        videoPlayer.setLoop(false);
        videoPlayer.setProgressCallBackSpaceMilliss(300);
        videoPlayer.getController().setTitle(ContentToString.getFileNameWithSuffix(ContentToString.getAbsolutePath(LocalVideo.this, Uri.parse(dataSource))));//视频标题(默认视图控制器横屏可见)
        videoPlayer.setDataSource(dataSource);//播放地址设置
        videoPlayer.startFullScreen();//开启全屏播放
        videoPlayer.prepareAsync();//开始异步准备播放

        mVideoPlayer = videoPlayer;

        WidgetFactory.bindDefaultControls(controller,true,true,this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        enterPicture(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        IWindowManager.getInstance().quitGlobaWindow();
        if(null!=mVideoPlayer){
            mVideoPlayer.setParentContext(null);
            mVideoPlayer.onDestroy();
        }
        try {
            unregisterReceiver(mReceiver);
        }catch (Throwable e){
            e.printStackTrace();
        }
        mReceiver=null;
    }

    @Override
    public void finish(){
        if(null!=mVideoPlayer){
            mVideoPlayer.setParentContext(null);
            mVideoPlayer.onDestroy();
        }
        try {
            unregisterReceiver(mReceiver);
        }catch (Throwable e){
            e.printStackTrace();
        }
        super.finish();
    }

    private void resetReceiver(){
        try {
            unregisterReceiver(mReceiver);
        }catch (Throwable e){
            e.printStackTrace();
        }finally {
            mReceiver=new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || !ACTION_MEDIA_CONTROL.equals(intent.getAction())) {
                        return;
                    }
                    final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
                    Logger.d(TAG,"controlType:"+controlType);
                    switch (controlType) {
                        case CONTROL_TYPE_PLAY:
                        case CONTROL_TYPE_PAUSE:
                        case CONTROL_TYPE_REPLAY:
                            if(null!=mVideoPlayer) mVideoPlayer.togglePlay();
                            break;
                    }
                }
            };
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resetReceiver();
        startFullScreen("1".equals(intent.getStringExtra("is_global")), intent.getData() != null? intent.getData().toString(): "");
        Log.d("videoURI",intent.getData().toString());
    }

    private void hideAllView(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final ArrayList<RemoteAction> actions = new ArrayList<>();
            final PendingIntent intent =
                    PendingIntent.getBroadcast(LocalVideo.this, requestCode,
                            new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType), 0);
            final Icon icon;
            icon = Icon.createWithResource(LocalVideo.this, iconId);
            actions.add(new RemoteAction(icon, title, title, intent));
            mBuilder.setActions(actions);
            setPictureInPictureParams(mBuilder.build());
        }
    }

    public void enterPicture(View view){
        if(null==mVideoPlayer) return;
        //判断当前设备是否支持画中画
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)){
            Toast.makeText(getApplicationContext(), "当前设备不支持画中画功能", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//>= Build.VERSION_CODES.N的设备进入画中画提高性能的API支持
            //这边是对画中画时添加一些按钮，如果不需要可以直接使用else里面的enterPictureInPictureMode方法直接进入画中画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Rational aspectRatio = new Rational(16, 9);
                mBuilder.setAspectRatio(aspectRatio).build();
//                builder.setAutoEnterEnabled(true);//提升动画流畅性,Android 12支持
                forbidCycle();//告诉父类忽视生命周期
                hideAllView();
                enterPictureInPictureMode(mBuilder.build());
            }else{
                forbidCycle();//告诉父类忽视生命周期
                hideAllView();
                enterPictureInPictureMode();
            }
        }
    }

    private void registerActionReceiver() {
        registerReceiver(mReceiver, new IntentFilter(ACTION_MEDIA_CONTROL));
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPicture(null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //画中画返回全屏会执行onresume
        if(!mVideoPlayer.isPlaying()){
            mVideoPlayer.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean inPictureInPictureMode = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            inPictureInPictureMode = isInPictureInPictureMode();
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "onStop -- inPictureInPictureMode=" + inPictureInPictureMode + " ,isEnteredPIPMode=" + isEnteredPIPMode + " ,isInPIPMode=" + isInPIPMode);
        }
        if (!inPictureInPictureMode && isInPIPMode && isEnteredPIPMode) {
            //满足此条件下认为是关闭了画中画界面
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "onStop -- 判断为PIP下关闭画中画");
            }
            enableCycle();
            onDestroy();

        }else if (inPictureInPictureMode && isInPIPMode && isEnteredPIPMode && videoPlayer != null) {
            //满足此条件下认为是画中画模式下锁屏
            mVideoPlayer.pause();
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "onStop -- 判断为PIP下锁屏");
            }
        }
//        Logger.d(TAG,"onStop");
//        try {
//            unregisterReceiver(mReceiver);
//        }catch (Throwable e){
//            e.printStackTrace();
//        }finally {
//            mReceiver=null;
////            if(null!=mVideoPlayer){
////                mVideoPlayer.onDestroy();
////            }
//        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        Logger.d(TAG, "onPictureInPictureModeChanged isPip = " + isInPictureInPictureMode+",newConfig:"+(null!=newConfig?newConfig.toString():""));
        if(null!=mVideoPlayer){
            if(isInPictureInPictureMode){
                isEnteredPIPMode = true;
                isInPIPMode = true;
                forbidCycle();//告诉父类忽视生命周期
                mVideoPlayer.enterPipWindow();//告诉播放器进入画中画场景
                mVideoPlayer.requestLayout();
                registerActionReceiver();//注册广播事件
            }else{
                if(null!=mReceiver){
                    resetReceiver();
                }
                isInPIPMode = false;
                enableCycle();//告诉父类关心生命周期
                mVideoPlayer.quitPipWindow();//告诉播放器退出画中画场景
                mVideoPlayer.requestLayout();

            }
        }
    }

    public static class pipListener extends ViewModel{
        private MutableLiveData<Boolean> enterOrExitPiPMode = new MutableLiveData<>();

        public MutableLiveData<Boolean> getEnterOrExitPiPMode() {
            return enterOrExitPiPMode;
        }

        public void setEnterOrExitPiPMode(boolean isEntering) {
            enterOrExitPiPMode.setValue(isEntering);
        }

        public void postEnterOrExitPiPMode(boolean isEntering) {
            enterOrExitPiPMode.postValue(isEntering);
        }
    }

//    private class MainActivityObserver implements LifecycleObserver {
//        private final pipListener viewModel;
//
//        MainActivityObserver(pipListener viewModel) {
//            this.viewModel = viewModel;
//        }
//
//        @OnLifecycleEvent(Lifecycle.Event.ON_START)
//        void onEnterForeground() {
//            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
//                viewModel.setEnterOrExitPiPMode(false);
//            }
//        }
//
//        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//        void onEnterBackground() {
//            // 其他逻辑
//        }
//    }
}
