package com.android.iplayer.widget.controls;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.android.iplayer.base.model.PlayerState;
import com.android.iplayer.base.widget.BaseControlWidget;
import com.android.iplayer.widget.R;

/**
 * created by hty
 * 2022/8/25
 * Desc:自定义组件-直播交互UI组件
 */
public class ControlLiveView extends BaseControlWidget implements View.OnClickListener{

    public ControlLiveView(Context context) {
        super(context);
    }

    @Override
    public int getLayoutId() {
        return R.layout.player_controller_live;
    }

    @Override
    public void initViews() {
        findViewById(R.id.live_mute).setOnClickListener(this);
        findViewById(R.id.live_fullscreen).setOnClickListener(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ImageView) findViewById(R.id.live_mute)).setImageResource(isSoundMute()?R.mipmap.ic_live_mute_true:R.mipmap.ic_live_mute_false);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.live_mute) {
            toggleMute();
        } else if (id == R.id.live_fullscreen) {
            toggleFullScreen();
        }
    }

    @Override
    public void onPlayerState(PlayerState state, String message) {
        super.onPlayerState(state, message);
        if(PlayerState.STATE_START==state){
            findViewById(R.id.live_controller).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMute(boolean isMute) {
        super.onMute(isMute);
        ((ImageView) findViewById(R.id.live_mute)).setImageResource(isMute?R.mipmap.ic_live_mute_true:R.mipmap.ic_live_mute_false);
    }
}
