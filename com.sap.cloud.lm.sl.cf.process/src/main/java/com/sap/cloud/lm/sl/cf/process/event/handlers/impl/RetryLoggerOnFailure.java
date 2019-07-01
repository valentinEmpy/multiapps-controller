package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.impl.event.FlowableEntityExceptionEventImpl;
import org.flowable.job.service.impl.persistence.entity.AbstractJobEntityImpl;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEngineEventUtils;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnJobRetryDecrementedQualifier;
import com.sap.cloud.lm.sl.cf.process.message.Messages;

@Component
@OnJobRetryDecrementedQualifier
public class RetryLoggerOnFailure implements FlowableEventHandler {

    private ProgressMessageService progressMessageService;

    @Inject
    public RetryLoggerOnFailure(ProgressMessageService progressMessageService) {
        this.progressMessageService = progressMessageService;
    }

    @Override
    public void handle(FlowableEvent event) {
        if (!(event instanceof FlowableEntityExceptionEventImpl)) {
            return;
        }

        FlowableEntityExceptionEventImpl entityExceptionEvent = (FlowableEntityExceptionEventImpl) event;
        AbstractJobEntityImpl job = (AbstractJobEntityImpl) entityExceptionEvent.getEntity();
        if (job.getRetries() != 0) {
            logRetryAsProgressMessage(entityExceptionEvent, job);
        }
    }

    private void logRetryAsProgressMessage(FlowableEntityExceptionEventImpl engineEvent, AbstractJobEntityImpl job) {
        String correlationId = FlowableEngineEventUtils.getVariable(engineEvent, Constants.VAR_CORRELATION_ID);
        String taskId = FlowableEngineEventUtils.getTaskId(engineEvent);
        String message = MessageFormat.format(Messages.NON_CONTENT_FAILURE_DETECTED_RETRYING, job.getRetries());
        progressMessageService
            .add(new ProgressMessage(correlationId, taskId, ProgressMessageType.INFO, message, new Timestamp(System.currentTimeMillis())));
    }

}
