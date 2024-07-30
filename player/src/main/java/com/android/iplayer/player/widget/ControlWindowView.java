package com.android.iplayer.player.widget;

import android.content.Context;

import com.android.iplayer.player.manager.IWindowManager;
import com.android.iplayer.widget.controls.AbstractControlWindowView;

public class ControlWindowView extends AbstractControlWindowView {

    public ControlWindowView(Context context) {
        super(context);
    }

    @Override
    public void onWindowFullScreen() {
        IWindowManager.getInstance().onClickWindow();
    }
}
