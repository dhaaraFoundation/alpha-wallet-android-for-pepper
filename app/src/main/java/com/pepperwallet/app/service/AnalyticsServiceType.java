package com.pepperwallet.app.service;

import com.pepperwallet.app.entity.ServiceErrorException;

public interface AnalyticsServiceType<T> {

    void track(String eventName);

    void track(String eventName, T event);

    void flush();

    void identify(String uuid);

    void recordException(ServiceErrorException e);
}