package com.sap.cloud.lm.sl.cf.process.listeners;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.RetryLoggerOnEntityEvent;

@Component("jobRetriesDecrementedListener")
public class JobRetriesDecrementedListener extends AbstractFlowableEventListener {

    @Inject
    private ProgressMessageService progressMessageService;
    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEventHandler eventHandler = new RetryLoggerOnEntityEvent(progressMessageService);
        eventHandler.handleIf(this::isJobRetriesDecrementedEvent);
        eventHandler.handle(event);
    }

    private boolean isJobRetriesDecrementedEvent(FlowableEvent event) {
        return event.getType()
            .equals(FlowableEngineEventType.JOB_RETRIES_DECREMENTED);
    }
    
    @Override
    public boolean isFailOnException() {
        return false;
    }

}
