package com.android.iplayer.widget.controls;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.iplayer.widget.OriginalResource;
import com.android.iplayer.widget.util.ScreenUtils;



public class DanmuWidget extends DanmuWidgetView {

    public DanmuWidget(@NonNull Context context, OriginalResource resource) {
        super(context, resource);
    }

    public DanmuWidget(@NonNull Context context, @Nullable AttributeSet attrs, OriginalResource resource) {
        super(context, attrs, resource);
    }

    public DanmuWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, OriginalResource resource) {
        super(context, attrs, defStyleAttr,resource);
    }

    @Override
    protected int getStatusBarHeight(Context context) {
        return ScreenUtils.getInstance(context).getStatusBarHeight(context);
    }

    @Override
    protected int getScreenHeight(){
        return ScreenUtils.getInstance(getContext()).getScreenHeight();
    }
}
