package com.android.iplayer.base.orientation;

import androidx.lifecycle.ViewModel;

public class OrientationData extends ViewModel {

    private boolean isShowMute;

    public void setIsShowMute(boolean isShowMute){
        this.isShowMute = isShowMute;
    }

    public boolean getIsShowMute(){
        return isShowMute;
    }
}

