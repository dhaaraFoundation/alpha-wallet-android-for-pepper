package com.pepperwallet.app.ui.widget;


import com.pepperwallet.app.entity.nftassets.NFTAsset;

import java.math.BigInteger;

public interface OnAssetSelectListener
{
    void onAssetSelected(BigInteger tokenId, NFTAsset asset, int position);
    void onAssetUnselected();
}
