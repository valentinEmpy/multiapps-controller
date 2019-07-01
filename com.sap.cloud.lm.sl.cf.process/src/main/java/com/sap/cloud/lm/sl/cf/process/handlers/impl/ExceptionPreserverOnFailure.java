package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEngineEventHandler;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.SLException;

public class ExceptionPreserverOnFailure extends FlowableEngineEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionPreserverOnFailure.class);

    private ProgressMessageService progressMessageService;

    public ExceptionPreserverOnFailure(ProgressMessageService progressMessageService) {
        handleIf(this::isFlowableExceptionEvent);
        this.progressMessageService = progressMessageService;
    }

    private boolean isFlowableExceptionEvent(FlowableEvent event) {
        return (event instanceof FlowableExceptionEvent);
    }

    @Override
    public void handleInternal(FlowableEvent event) {
        try {
            FlowableExceptionEvent exceptionEvent = (FlowableExceptionEvent) event;
            logStacktrace(exceptionEvent);
            preserveExceptionAsProgressMessage(event, exceptionEvent.getCause()
                .getMessage());
        } catch (SLException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    private void logStacktrace(FlowableExceptionEvent exceptionEvent) {
        String exceptionStackTrace = ExceptionUtils.getStackTrace(exceptionEvent.getCause());
        LOGGER.error(exceptionStackTrace);
    }

    private void preserveExceptionAsProgressMessage(FlowableEvent event, String exceptionMessage) {
        if (exceptionMessage == null) {
            return;
        }
        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        String correlationId = getVariable(engineEvent, Constants.VAR_CORRELATION_ID);
        String errorMessage = MessageFormat.format(Messages.EXCEPTION_OCCURED_ERROR_MSG, exceptionMessage);
        progressMessageService.add(new ProgressMessage(correlationId, getTaskId(engineEvent), ProgressMessageType.ERROR, errorMessage,
            new Timestamp(System.currentTimeMillis())));
    }

}
