package com.pepperwallet.app.ui.widget;

import com.pepperwallet.app.entity.tokens.Token;

public interface OnTokenManageClickListener
{
    void onTokenClick(Token token, int position, boolean isChecked);
}
