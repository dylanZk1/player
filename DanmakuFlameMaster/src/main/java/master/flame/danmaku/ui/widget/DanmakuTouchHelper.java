package master.flame.danmaku.ui.widget;

import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;

/**
 * Created by kmfish on 2015/1/25.
 */
public class DanmakuTouchHelper{
    private final GestureDetector mTouchDelegate;
    private IDanmakuView danmakuView;
    private RectF mDanmakuBounds;
    private float mXOff;
    private float mYOff;

    private final android.view.GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            if (danmakuView != null) {
                IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
                if (onDanmakuClickListener != null) {
                    mXOff = danmakuView.getXOff();
                    mYOff = danmakuView.getYOff();
                    performViewClick("Down",event);
                    return true;
                }
            }
            performViewClick("Down",event);
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            IDanmakus clickDanmakus = touchHitDanmaku(event.getX(), event.getY());
            boolean isEventConsumed = false;
            if (null != clickDanmakus && !clickDanmakus.isEmpty()) {
                isEventConsumed = performDanmakuClick(clickDanmakus, false);
            }
            if (!isEventConsumed) {
                isEventConsumed = performViewClick("SingleTap",event);
            }
            return isEventConsumed;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
            if (onDanmakuClickListener == null) {
                return;
            }
            mXOff = danmakuView.getXOff();
            mYOff = danmakuView.getYOff();
            IDanmakus clickDanmakus = touchHitDanmaku(event.getX(), event.getY());
            if (null != clickDanmakus && !clickDanmakus.isEmpty()) {
                performDanmakuClick(clickDanmakus, true);
            }else{
                performViewLongClick("LongClick",event);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e){
            return performViewClick("DoubleTap",e);
        }

        @Override
        public boolean onScroll(MotionEvent e1 , MotionEvent e2 ,float distanceX , float distanceY){
            return performViewClick("Scroll",e1,e2,distanceX,distanceY);
        }

    };

    private DanmakuTouchHelper(IDanmakuView danmakuView) {
        this.danmakuView = danmakuView;
        this.mDanmakuBounds = new RectF();
        this.mTouchDelegate = new GestureDetector(((View) danmakuView).getContext(), mOnGestureListener);
    }

    public static synchronized DanmakuTouchHelper instance(IDanmakuView danmakuView) {
        return new DanmakuTouchHelper(danmakuView);
    }

    public boolean onTouchEvent(MotionEvent event) {
        performViewTouch("Touch",event);
        return mTouchDelegate.onTouchEvent(event);
    }

    private boolean performDanmakuClick(IDanmakus danmakus, boolean isLongClick) {
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            if (isLongClick) {
                return onDanmakuClickListener.onDanmakuLongClick(danmakus);
            } else {
                return onDanmakuClickListener.onDanmakuClick(danmakus);
            }
        }
        return false;
    }

    private boolean performViewTouch(String tag, Object ...extra){
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            return onDanmakuClickListener.onViewTouch(tag,extra);
        }
        return false;
    }

    private boolean performViewClick(String tag, Object ...extra) {
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            return onDanmakuClickListener.onViewClick(danmakuView,tag,extra);
        }
        return false;
    }

    private void performViewLongClick(String tag, Object ...extra){
        IDanmakuView.OnDanmakuClickListener onDanmakuClickListener = danmakuView.getOnDanmakuClickListener();
        if (onDanmakuClickListener != null) {
            onDanmakuClickListener.onViewLongClick(danmakuView,tag,extra);
        }
    }

    private IDanmakus touchHitDanmaku(final float x, final float y) {
        final IDanmakus hitDanmakus = new Danmakus();
        mDanmakuBounds.setEmpty();

        IDanmakus danmakus = danmakuView.getCurrentVisibleDanmakus();
        if (null != danmakus && !danmakus.isEmpty()) {
            danmakus.forEachSync(new IDanmakus.DefaultConsumer<BaseDanmaku>() {
                @Override
                public int accept(BaseDanmaku danmaku) {
                    if (null != danmaku) {
                        mDanmakuBounds.set(danmaku.getLeft(), danmaku.getTop(), danmaku.getRight(), danmaku.getBottom());
                        if (mDanmakuBounds.intersect(x - mXOff, y - mYOff, x + mXOff, y + mYOff)) {
                            hitDanmakus.addItem(danmaku);
                        }
                    }
                    return ACTION_CONTINUE;
                }
            });
        }

        return hitDanmakus;
    }
}
