package com.sap.cloud.lm.sl.cf.process.event.handlers;

import org.flowable.engine.delegate.DelegateExecution;

public interface FlowableExecutionEventHandler {

    public void handle(DelegateExecution context);
}
