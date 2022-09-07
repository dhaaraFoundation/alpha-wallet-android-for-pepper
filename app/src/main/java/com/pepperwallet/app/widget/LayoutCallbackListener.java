package com.pepperwallet.app.widget;

import android.view.View;

public interface LayoutCallbackListener
{
    void onLayoutShrunk();
    void onLayoutExpand();
    void onInputDoneClick(View view);
}
