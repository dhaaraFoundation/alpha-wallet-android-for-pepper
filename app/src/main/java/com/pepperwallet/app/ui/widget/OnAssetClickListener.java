package com.pepperwallet.app.ui.widget;


import android.util.Pair;

import com.pepperwallet.app.entity.nftassets.NFTAsset;

import java.math.BigInteger;

public interface OnAssetClickListener
{
    void onAssetClicked(Pair<BigInteger, NFTAsset> item);
}
