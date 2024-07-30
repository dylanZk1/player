package com.android.iplayer.widget.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.iplayer.base.controller.ControlWrapper;
import com.android.iplayer.base.media.IMediaPlayer;

import com.android.iplayer.base.model.PlayerState;
import com.android.iplayer.base.utils.PlayerUtils;
import com.android.iplayer.widget.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.IOUtils;

/**
 * created by lz
 * 2022/12/21
 * Desc:弹幕解析
 */
public abstract class AbstractPaserView<T extends BaseDanmakuParser> extends FrameLayout {

    private T mParser;//解析器对象
    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;
    private boolean isInit=false;
    private SetController controller;
    private danmuContents danmu;
    private int orientation = IMediaPlayer.ORIENTATION_PORTRAIT;
    private final BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        private Drawable mDrawable;

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
                new Thread() {

                    @Override
                    public void run() {
                        String url = "http://www.bilibili.com/favicon.ico";
                        InputStream inputStream = null;
                        Drawable drawable = mDrawable;
                        if(drawable == null) {
                            try {
                                URLConnection urlConnection = new URL(url).openConnection();
                                inputStream = urlConnection.getInputStream();
                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
                                mDrawable = drawable;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(inputStream);
                            }
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, 100, 100);
                            danmaku.text = createSpannable(drawable);
                            if(mDanmakuView != null) {
                                mDanmakuView.invalidateDanmaku(danmaku, false);
                            }
                        }
                    }
                }.start();
            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
//            danmaku.cache.erase();
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
        }
    };

    public AbstractPaserView(@NonNull Context context) {
        this(context,null);
    }

    public AbstractPaserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AbstractPaserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_danmu_paser_layout, this);
        mDanmakuContext = DanmakuContext.create();
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    public void setOrientation(int orientation){
        this.orientation = orientation;
    }

    public void setDanmakuMargin(int margin){
        if(mDanmakuView!=null){
            mDanmakuView.getConfig().setDanmakuMargin(margin);
        }
    }

    /**
     * 添加一条自己发布的留言到字幕上
     * @param comment
     */
    protected void addInputDanmaku(String comment) {
        BaseDanmaku danmaku = getDanmaku();
        if(null!=danmaku){
            try {
                danmaku.text = URLDecoder.decode(comment, "utf-8");
                danmaku.padding = 6;
                danmaku.priority = 2;  // 一定会显示, 一般用于本机发送的弹幕
                danmaku.isLive = true;
                danmaku.setTime(mDanmakuView.getCurrentTime());//立即显示
                danmaku.textSize = PlayerUtils.getInstance().dpToPxInt(16f); //文本弹幕字体大小
                danmaku.textColor = Color.parseColor("#FF5000");
                mDanmakuView.addDanmaku(danmaku);//调用这个方法，添加字幕到控件，开始滚动
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加一条字幕到控件
     * @param cs
     * @param islive
     */
    private void addDanmaku(CharSequence cs, boolean islive) {
        if(TextUtils.isEmpty(cs)) return;
        BaseDanmaku danmaku = getDanmaku();
        if(null!=danmaku){
            try {
                danmaku.text = cs;
                danmaku.padding = 6;
                danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
                danmaku.isLive = islive;
                danmaku.setTime(mDanmakuView.getCurrentTime() + 2000);//多长时间后加入弹幕组合
                danmaku.textSize = PlayerUtils.getInstance().dpToPxInt(16f); //文本弹幕字体大小
                danmaku.textColor = Color.parseColor("#E6FFFFFF"); //文本的颜色
                danmaku.textShadowColor =Color.parseColor("#80000000"); //文本弹幕描边的颜色
                //danmaku.underlineColor = Color.DKGRAY; //文本弹幕下划线的颜色
                //danmaku.borderColor = Color.parseColor("#80000000"); //边框的颜色
                mDanmakuView.addDanmaku(danmaku);//调用这个方法，添加字幕到控件，开始滚动
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回弹幕的可用状态
     * @return
     */
    private BaseDanmaku getDanmaku() {
        if(null==mDanmakuView) return null;
        if(null==mParser) {
            mParser = createParser(null);
        }
        if(null==mDanmakuContext) {
            mDanmakuContext=DanmakuContext.create();
        }
        return mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
    }



    /**
     * 弹幕是否已经初始化了
     * @return
     */
    public boolean isInit() {
        return isInit;
    }

    /**
     * 初始化弹幕和设置并自动开始滚动显示
     */
    public void initDanmaku() {

        if(null!=mDanmakuView&&isInit){
            return;
        }
        //实例化
        mDanmakuView = findViewById(R.id.sv_danmaku);
        mDanmakuView.show();
        if(null==mDanmakuContext){
            mDanmakuContext = DanmakuContext.create();
        }
        // 设置滚动方向、最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3); // 设置从右向左滚动，最大同时显示2行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, false);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        //普通文本弹幕描边设置样式
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3) //描边的厚度
                .setDuplicateMergingEnabled(false) //如果是图文混合编排编排，最后不要描边
                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter)
                .setScrollSpeedFactor(2.2f) //弹幕的速度。注意！此值越小，速度越快！值越大，速度越慢。// by phil
                .setScaleTextSize(orientation == IMediaPlayer.ORIENTATION_PORTRAIT?0.84f:1.0f)  //缩放的值，默认为竖屏时的比例
                .setDanmakuMargin(orientation == IMediaPlayer.ORIENTATION_PORTRAIT?10:20) //字体间距，默认为竖屏时的大小
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuBold(true)
                .setTypeface(getContext(), "default")
        ;
        if(null==mParser){
            mParser = createParser(danmu.content());
        }

        mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {

            @Override
            public boolean onDanmakuClick(IDanmakus danmakus) {
                return controller.onDanmuClick(danmakus);
            }

            @Override
            public boolean onDanmakuLongClick(IDanmakus danmakus) {
                return controller.onDanmuLongClick(danmakus);
            }

            @Override
            public boolean onViewClick(IDanmakuView view, String tag, Object... extra) {
                controller.onController(tag,extra);
                return true;
            }

            @Override
            public void onViewLongClick(IDanmakuView view, String tag, Object... extra) {
                controller.onController(tag,mDanmakuView,extra);
            }

            @Override
            public boolean onViewTouch(String tag, Object... extra) {
                controller.onController(tag,extra);
                return false;
            }
        });

        mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
            @Override
            public void updateTimer(DanmakuTimer timer) {
                 controller.updateTimer(timer);
            }

            @Override
            public void drawingFinished() {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void prepared() {//准备完成了
                isInit=true;
                if(null!=mDanmakuView){
                    if(null!=controller.getControlWrapper() && controller.getControlWrapper().getCurrentState() != PlayerState.STATE_PAUSE){
                        mDanmakuView.start();
                    }
                }
            }
        });
        mDanmakuView.enableDanmakuDrawingCache(true);//保存绘制的缓存
        mDanmakuView.showFPS(false);//显示实时帧率，调试模式下开启
        mDanmakuView.prepare(mParser, mDanmakuContext);
    }

    /**
     * 添加单条弹幕数据
     * @param content 弹幕文本内容
     * @param isOneself 是否是自己发送的
     */
    public void addDanmuItem(String content,boolean isOneself){
        if(isOneself){
            addInputDanmaku(content);
        }else{
            addDanmaku(content,false);
        }
    }

    public void onResume(){
        if(null!=mDanmakuView){
            mDanmakuView.resume();
        }
    }

    public void onPause(){
        if(null!=mDanmakuView){
            mDanmakuView.pause();
        }
    }

    public void onReset(){
        releaseDanmaku();
    }

    /**
     * 销毁和释放弹幕相关所有资源资源
     */
    public void releaseDanmaku() {
        if (null != mDanmakuView) {
            if (mDanmakuView.isPrepared()) {
                mDanmakuView.stop();//停止弹幕
            }
            mDanmakuView.stop();
            mDanmakuView.removeAllDanmakus(true);
            mDanmakuView.release();//释放弹幕资源
            mDanmakuView.clearDanmakusOnScreen();
        }
        isInit=false;
    }

    public void onDestroy(){
        releaseDanmaku();
        mDanmakuView=null;
    }

    public void setController(SetController controller){
        this.controller = controller;
    }

    public interface SetController {
        void onController(String tag, Object ...extra);

        boolean onDanmuClick(IDanmakus danmakus);

        boolean onDanmuLongClick(IDanmakus danmakus);

        void updateTimer(DanmakuTimer timer);

        ControlWrapper getControlWrapper();
    }

    public void start(long duraion){
        mDanmakuView.start(duraion);
    }

    public interface danmuContents{

        InputStream content();
    }

    public void setDanmuContent(danmuContents danmu){
        this.danmu = danmu;
    }

    public void setTextsizeScale(float textsizeScale){
        if(mDanmakuContext!=null){
            mDanmakuContext.setScaleTextSize(textsizeScale);
        }
    }

    protected abstract T createParser(InputStream stream);
}