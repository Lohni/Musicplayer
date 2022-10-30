package com.example.musicplayer.interfaces;

public interface NavigationControlInterface {
    void isDrawerEnabledListener(boolean state);
    void setHomeAsUpEnabled(boolean state);
    void setHomeAsUpIndicator(int resId);
    void setToolbarTitle(String title);
    void onBackPressedListener();
    void setToolbarBackground(boolean scrolling);
}
