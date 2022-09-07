package com.pepperwallet.app.repository;

import com.pepperwallet.app.entity.OnRampContract;
import com.pepperwallet.app.entity.tokens.Token;

public interface OnRampRepositoryType {
    String getUri(String address, Token token);

    OnRampContract getContract(Token token);
}
