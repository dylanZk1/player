package com.zlm.subtitlelibrary.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @Description: 字幕视图
 * @author: zhangliangming
 * @date: 2019-01-17 22:17
 **/
@SuppressLint("AppCompatCustomView")
public class SubtitleView extends TextView {

    public SubtitleView(Context context) {
        super(context);
        init(context);
    }

    public SubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

    }
}
