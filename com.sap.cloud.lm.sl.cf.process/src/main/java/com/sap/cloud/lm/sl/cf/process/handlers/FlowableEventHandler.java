package com.sap.cloud.lm.sl.cf.process.handlers;

import java.util.function.Predicate;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;

public abstract class FlowableEventHandler {

    protected FlowableEventHandler next;
    private Predicate<FlowableEvent> shouldHandle;

    public void setNext(FlowableEventHandler next) {
        this.next = next;
    }

    public void handleIf(Predicate<FlowableEvent> shouldHandle) {
        if (this.shouldHandle != null) {
            this.shouldHandle.and(shouldHandle);
            return;
        }
        this.shouldHandle = shouldHandle;
    }

    public void handle(FlowableEvent event) {
        if (shouldHandle.test(event)) {
            handleInternal(event);
        }
        handleNext(event);
    }

    protected void handleNext(FlowableEvent event) {
        if (next != null) {
            next.handle(event);
        }
    }

    protected abstract void handleInternal(FlowableEvent event);

}
