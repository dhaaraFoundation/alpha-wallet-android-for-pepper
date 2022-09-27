package com.pepperwallet.app.widget;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pepperwallet.app.R;
import com.pepperwallet.app.entity.tokens.Token;
import com.pepperwallet.app.service.TickerService;
import com.pepperwallet.app.service.TokensService;

import org.web3j.crypto.Keys;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TokenInfoHeaderView extends LinearLayout {
    private final TokenIcon icon;
    private final TextView amount;
    private final TextView symbol;
    private final TextView marketValue;
    private final TextView priceChange;
    private   TokenIcon token_icon;

    public TokenInfoHeaderView(Context context)
    {
        super(context);
        inflate(context, R.layout.item_token_info_header, this);
        icon = findViewById(R.id.token_icon);
        amount = findViewById(R.id.token_amount);
        symbol = findViewById(R.id.token_symbol);
        marketValue = findViewById(R.id.market_value);
        priceChange = findViewById(R.id.price_change);
    }

    public TokenInfoHeaderView(Context context, Token token, TokensService svs)
    {
        this(context);
//        icon.bindData(token, svs);
        String new_address = Keys.toChecksumAddress(token.tokenInfo.address);
        Log.d("new_address", new_address);
        if (new_address != null)
        {
            String imageUrl = "https://raw.githubusercontent.com/analogchain/explorer/main/assets/blockchain/rabbit/" + new_address+ "/logo.png";
            Log.d("url", imageUrl);
            icon.loadImage(imageUrl);
        }
//        if (!token.isEthereum()) icon.setChainIcon(token.tokenInfo.chainId);// used for small icon
        setAmount(token.getFixedFormattedBalance());
        setSymbol(token.tokenInfo.symbol);
        //obtain from ticker
        Pair<Double, Double> pricePair = svs.getFiatValuePair(token.tokenInfo.chainId, token.getAddress());

        setMarketValue(pricePair.first);
        setPriceChange(pricePair.second);
    }

    public void setAmount(String text)
    {
        amount.setText(text);
    }

    public void setSymbol(String text)
    {
        symbol.setText(text);
    }

    public void setMarketValue(double value)
    {
        String formattedValue = TickerService.getCurrencyString(value);
        marketValue.setText(formattedValue);
    }

    /**
     *
     * Automatically formats the string based on the passed value
     *
     * **/
    private void setPriceChange(double percentChange24h)
    {
        try {
            priceChange.setVisibility(View.VISIBLE);
            int color = ContextCompat.getColor(getContext(), percentChange24h < 0 ? R.color.negative : R.color.positive);
            BigDecimal percentChangeBI = BigDecimal.valueOf(percentChange24h).setScale(3, RoundingMode.DOWN);
            String formattedPercents = (percentChange24h < 0 ? "(" : "(+") + percentChangeBI + "%)";
            priceChange.setText(formattedPercents);
            priceChange.setTextColor(color);
        } catch (Exception ex) { /* Quietly */ }
    }
}
