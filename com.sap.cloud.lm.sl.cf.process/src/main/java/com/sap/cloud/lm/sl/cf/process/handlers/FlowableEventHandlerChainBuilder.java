package com.sap.cloud.lm.sl.cf.process.handlers;

import java.util.function.Predicate;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;

public class FlowableEventHandlerChainBuilder {

    private FlowableEventHandler eventHandler;

    public static FlowableEventHandlerChainBuilder of(FlowableEventHandler eventHandler) {
        return new FlowableEventHandlerChainBuilder(eventHandler);
    }

    private FlowableEventHandlerChainBuilder(FlowableEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public FlowableEventHandlerChainBuilder chainWith(FlowableEventHandler eventHandler) {
        return chainHandlers(this.eventHandler, eventHandler);
    }

    private FlowableEventHandlerChainBuilder chainHandlers(FlowableEventHandler rootEventHandler, FlowableEventHandler eventHandler) {
        if (rootEventHandler.next == null) {
            rootEventHandler.setNext(eventHandler);
            return this;
        }
        return chainHandlers(rootEventHandler.next, eventHandler);
    }

    public FlowableEventHandlerChainBuilder handleLastIf(Predicate<FlowableEvent> shouldHandle) {
        return handleLastIf(eventHandler, shouldHandle);
    }

    private FlowableEventHandlerChainBuilder handleLastIf(FlowableEventHandler eventHandler, Predicate<FlowableEvent> shouldHandle) {
        if (eventHandler.next == null) {
            eventHandler.handleIf(shouldHandle);
            return this;
        }
        return handleLastIf(eventHandler.next, shouldHandle);
    }

    public FlowableEventHandlerChainBuilder handleAllIf(Predicate<FlowableEvent> shouldHandle) {
        return handleAllIf(eventHandler, shouldHandle);
    }

    private FlowableEventHandlerChainBuilder handleAllIf(FlowableEventHandler eventHandler, Predicate<FlowableEvent> shouldHandle) {
        if (eventHandler != null) {
            eventHandler.handleIf(shouldHandle);
            return handleAllIf(eventHandler.next, shouldHandle);
        }
        return this;
    }

    public FlowableEventHandler build() {
        return eventHandler;
    }
}