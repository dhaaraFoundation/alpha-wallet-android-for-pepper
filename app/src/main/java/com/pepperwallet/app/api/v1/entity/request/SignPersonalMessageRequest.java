package com.pepperwallet.app.api.v1.entity.request;

import com.pepperwallet.app.api.v1.entity.ApiV1;
import com.pepperwallet.token.entity.EthereumMessage;
import com.pepperwallet.token.entity.SignMessageType;
import com.pepperwallet.token.entity.Signable;

public class SignPersonalMessageRequest extends ApiV1Request
{
    private final String address;
    private final String message;

    public SignPersonalMessageRequest(String requestUrl)
    {
        super(requestUrl);
        this.message = this.requestUrl.queryParameter(ApiV1.RequestParams.MESSAGE);
        this.address = this.requestUrl.queryParameter(ApiV1.RequestParams.ADDRESS);
    }

    public String getMessage()
    {
        return message;
    }

    public String getAddress()
    {
        return address;
    }

    public Signable getSignable()
    {
        return new EthereumMessage(
                message,
                metadata.name,
                -1,
                SignMessageType.SIGN_PERSONAL_MESSAGE);
    }
}
