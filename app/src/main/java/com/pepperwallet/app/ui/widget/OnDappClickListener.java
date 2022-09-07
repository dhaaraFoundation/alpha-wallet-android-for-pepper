package com.pepperwallet.app.ui.widget;

import java.io.Serializable;

import com.pepperwallet.app.entity.DApp;

public interface OnDappClickListener extends Serializable {
    void onDappClick(DApp dapp);
}
