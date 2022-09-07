package com.pepperwallet.app.ui.widget.holder;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pepperwallet.app.ui.widget.entity.ManageTokensData;

public class ManageTokensHolder extends BinderViewHolder<ManageTokensData> {
    public static final int VIEW_TYPE = 2015;

    Button button;

    @Override
    public void bind(@Nullable ManageTokensData data, @NonNull Bundle addition) {

    }

    public ManageTokensHolder(int res_id, ViewGroup parent) {
        super(res_id, parent);
    }
}
