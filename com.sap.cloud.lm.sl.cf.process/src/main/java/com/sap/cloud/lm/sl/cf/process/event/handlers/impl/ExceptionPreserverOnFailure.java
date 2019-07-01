package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableExceptionEvent;
import org.flowable.common.engine.impl.event.FlowableEntityExceptionEventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEngineEventUtils;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnErrorJobQualifier;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.SLException;

@Component
@OnErrorJobQualifier
public class ExceptionPreserverOnFailure implements FlowableEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionPreserverOnFailure.class);
    private ProgressMessageService progressMessageService;

    @Inject
    public ExceptionPreserverOnFailure(ProgressMessageService progressMessageService) {
        this.progressMessageService = progressMessageService;
    }

    @Override
    public void handle(FlowableEvent event) {
        if (event instanceof FlowableEntityExceptionEventImpl) {
            preserveException(event);
        }
    }

    private void preserveException(FlowableEvent event) {
        try {
            FlowableEntityExceptionEventImpl entityExceptionEvent = (FlowableEntityExceptionEventImpl) event;
            logStacktrace(entityExceptionEvent);
            preserveExceptionMessageAsProgressMessage(entityExceptionEvent);
        } catch (SLException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    private void logStacktrace(FlowableExceptionEvent exceptionEvent) {
        String exceptionStackTrace = ExceptionUtils.getStackTrace(exceptionEvent.getCause());
        LOGGER.error(exceptionStackTrace);
    }

    private void preserveExceptionMessageAsProgressMessage(FlowableEntityExceptionEventImpl entityExceptionEvent) {
        if (entityExceptionEvent.getCause()
            .getMessage() == null) {
            return;
        }
        String correlationId = FlowableEngineEventUtils.getVariable(entityExceptionEvent, Constants.VAR_CORRELATION_ID);
        String errorMessage = MessageFormat.format(Messages.EXCEPTION_OCCURED_ERROR_MSG, entityExceptionEvent.getCause()
            .getMessage());
        progressMessageService.add(new ProgressMessage(correlationId, FlowableEngineEventUtils.getTaskId(entityExceptionEvent),
            ProgressMessageType.ERROR, errorMessage, new Timestamp(System.currentTimeMillis())));
    }

}
