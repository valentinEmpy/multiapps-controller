package com.sap.cloud.lm.sl.cf.process.listeners;

import java.util.List;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnErrorJobQualifier;

@Component("errorJobListener")
public class ErrorJobListener extends AbstractFlowableEventListener {

    @Inject
    @OnErrorJobQualifier
    List<FlowableEventHandler> eventHandlers;

    @Override
    public void onEvent(FlowableEvent event) {
        if (!isJobExecutionFailure(event)) {
            return;
        }
        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        for (FlowableEventHandler eventHandler : eventHandlers) {
            eventHandler.handle(engineEvent);
        }
    }

    private boolean isJobExecutionFailure(FlowableEvent event) {
        return event.getType()
            .equals(FlowableEngineEventType.JOB_EXECUTION_FAILURE);
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

}
