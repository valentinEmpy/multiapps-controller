package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.job.service.impl.persistence.entity.AbstractJobEntityImpl;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEngineEventHandler;
import com.sap.cloud.lm.sl.cf.process.message.Messages;

public class RetryLoggerOnEntityEvent extends FlowableEngineEventHandler {

    private ProgressMessageService progressMessageService;

    public RetryLoggerOnEntityEvent(ProgressMessageService progressMessageService) {
        handleIf(this::isFlowableExceptionEntityEvent);
        this.progressMessageService = progressMessageService;
    }

    private boolean isFlowableExceptionEntityEvent(FlowableEvent event) {
        return (event instanceof FlowableEntityEvent);
    }

    @Override
    protected void handleInternal(FlowableEvent event) {
        FlowableEntityEvent entityEvent = (FlowableEntityEvent) event;
        AbstractJobEntityImpl job = (AbstractJobEntityImpl) entityEvent.getEntity();
        if (job.getRetries() != 0) {
            logRetryAsProgressMessage((FlowableEngineEvent) event, job);
        }
    }

    private void logRetryAsProgressMessage(FlowableEngineEvent engineEvent, AbstractJobEntityImpl job) {
        String correlationId = getVariable(engineEvent, Constants.VAR_CORRELATION_ID);
        String taskId = getTaskId(engineEvent);
        String message = MessageFormat.format(Messages.NON_CONTENT_FAILURE_DETECTED_RETRYING, job.getRetries());
        progressMessageService
            .add(new ProgressMessage(correlationId, taskId, ProgressMessageType.INFO, message, new Timestamp(System.currentTimeMillis())));
    }
}
