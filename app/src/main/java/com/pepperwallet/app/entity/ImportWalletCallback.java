package com.pepperwallet.app.entity;
import com.pepperwallet.app.entity.cryptokeys.KeyEncodingType;
import com.pepperwallet.app.service.KeyService;

public interface ImportWalletCallback
{
    void walletValidated(String address, KeyEncodingType type, KeyService.AuthenticationLevel level);
}
