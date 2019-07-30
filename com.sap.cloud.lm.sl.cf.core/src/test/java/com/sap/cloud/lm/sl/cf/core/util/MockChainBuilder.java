package com.sap.cloud.lm.sl.cf.core.util;

import org.mockito.Mockito;

public class MockChainBuilder<T> {

    private T object;
    private Class<T> clasz;

    public MockChainBuilder(T object, Class<T> clasz) {
        this.object = object;
        this.clasz = clasz;
    }

    public MockChainBuilder<T> on(MockMethodCall<T> mockMethodCall) {
        T mock = Mockito.mock(clasz);
        Mockito.when(mockMethodCall.call(object))
            .thenReturn(mock);
        this.object = mock;
        return this;
    }

    public T get() {
        return this.object;
    }

    public interface MockMethodCall<T> {

        T call(T mock);
    }
}
