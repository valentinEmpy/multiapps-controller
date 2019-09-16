package com.sap.cloud.lm.sl.cf.core.util;

import java.util.function.Consumer;

import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MockBuilder<T> {

    private T mock;
    private Class<T> mockClass;

    public MockBuilder(T mock, Class<T> mockClass) {
        this.mock = mock;
        this.mockClass = mockClass;
    }

    public MockBuilder<T> on(MockMethodCall<T> mockMethodCall) {
        return on(mockMethodCall, null);
    }

    public MockBuilder<T> on(MockMethodCall<T> mockMethodCall, Consumer<InvocationOnMock> invocationCosumer) {
        T callResult = mockMethodCall.performOn(mock);
        if (callResult != null && callResult != mock) {
            this.mock = callResult;
            return this;
        }
        initNewMock(mockMethodCall, invocationCosumer);
        return this;
    }

    private void initNewMock(MockMethodCall<T> mockMethodCall, Consumer<InvocationOnMock> invocationConsumer) {
        T newMock = Mockito.mock(mockClass, Answers.RETURNS_SELF);
        Mockito.when(mockMethodCall.performOn(mock))
               .thenAnswer(getAnswer(newMock, invocationConsumer));
        this.mock = newMock;
    }

    private Answer<T> getAnswer(T answer, Consumer<InvocationOnMock> invocationConsumer) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocation) throws Throwable {
                if (invocationConsumer != null) {
                    invocationConsumer.accept(invocation);
                }
                return answer;
            }
        };
    }

    public T build() {
        return this.mock;
    }

    public interface MockMethodCall<T> {

        T performOn(T mock);
    }
}
