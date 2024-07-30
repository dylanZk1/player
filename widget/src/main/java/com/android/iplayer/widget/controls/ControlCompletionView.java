package com.android.iplayer.widget.controls;

import android.content.Context;
import com.android.iplayer.widget.R;
import com.android.iplayer.base.widget.BaseControlWidget;
import com.android.iplayer.base.model.PlayerState;

/**
 * created by hty
 * 2022/8/22
 * Desc:UI控制器-播放完成
 */
public class ControlCompletionView extends BaseControlWidget {

    public ControlCompletionView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_control_completion;
    }

    @Override
    public void initViews() {
        hide();
        setOnClickListener(view -> {
            if(null!=mControlWrapper) mControlWrapper.togglePlay();
        });
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        if (state == PlayerState.STATE_COMPLETION) {//播放结束
            if (!isWindowScene() && !isPreViewScene()) {//窗口播放模式/试看模式不显示
                show();
            }
        } else {
            hide();
        }
    }

    @Override
    public void onOrientation(int direction) {}
}