package com.sap.cloud.lm.sl.cf.process.event.handlers;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;

public interface FlowableEventHandler {

    public void handle(FlowableEvent event);
}
