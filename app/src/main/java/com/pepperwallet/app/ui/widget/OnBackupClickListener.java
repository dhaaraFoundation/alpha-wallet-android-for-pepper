package com.pepperwallet.app.ui.widget;

import android.view.View;

import com.pepperwallet.app.entity.Wallet;

public interface OnBackupClickListener {
    void onBackupClick(View view, Wallet wallet);
}
