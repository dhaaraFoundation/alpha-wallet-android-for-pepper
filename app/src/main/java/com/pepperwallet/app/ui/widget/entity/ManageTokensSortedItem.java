package com.pepperwallet.app.ui.widget.entity;

import com.pepperwallet.app.ui.widget.holder.ManageTokensHolderOld;


public class ManageTokensSortedItem extends SortedItem<ManageTokensData> {

    public ManageTokensSortedItem(ManageTokensData data) {
        super(ManageTokensHolderOld.VIEW_TYPE, data, new TokenPosition(Long.MAX_VALUE));
    }

    @Override
    public boolean areContentsTheSame(SortedItem newItem)
    {
        return false;
    }

    @Override
    public boolean areItemsTheSame(SortedItem other)
    {
        return other.viewType == viewType;
    }
}
