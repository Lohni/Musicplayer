package com.example.musicplayer.utils;

public interface NavigationControlInterface {
    void isDrawerEnabledListener(boolean state);
    void setHomeAsUpEnabled(boolean state);
    void setHomeAsUpIndicator(int resId);
    void setToolbarTitle(String title);
}
