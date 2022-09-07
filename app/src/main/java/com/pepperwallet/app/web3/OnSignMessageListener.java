package com.pepperwallet.app.web3;

import com.pepperwallet.token.entity.EthereumMessage;

public interface OnSignMessageListener {
    void onSignMessage(EthereumMessage message);
}
