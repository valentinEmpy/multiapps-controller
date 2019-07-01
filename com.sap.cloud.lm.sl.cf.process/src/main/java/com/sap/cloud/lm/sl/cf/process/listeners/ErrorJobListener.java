package com.sap.cloud.lm.sl.cf.process.listeners;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandlerChainBuilder;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.ExceptionPreserverOnFailure;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.ProcessClientReleaser;

@Component("errorJobListener")
public class ErrorJobListener extends AbstractFlowableEventListener {

    @Inject
    protected CloudControllerClientProvider clientProvider;

    @Inject
    private ProgressMessageService progressMessageService;

    @Inject
    private FileService fileService;

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEventHandler eventHandler = FlowableEventHandlerChainBuilder.of(new ExceptionPreserverOnFailure(progressMessageService))
            .chainWith(new ProcessClientReleaser(fileService, clientProvider))
            .handleAllIf(this::isJobExecutionFailure)
            .build();
        eventHandler.handle(event);
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
