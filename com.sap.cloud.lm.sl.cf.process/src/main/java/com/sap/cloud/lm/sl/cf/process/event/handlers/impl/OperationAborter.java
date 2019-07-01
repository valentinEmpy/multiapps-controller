package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEngineEventUtils;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnAbortProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

@Component
@OnAbortProcessQualifier
public class OperationAborter implements FlowableEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationAborter.class);
    private OperationDao operationDao;

    @Inject
    public OperationAborter(OperationDao operationDao) {
        this.operationDao = operationDao;
    }

    @Override
    public void handle(FlowableEvent event) {
        if (event instanceof FlowableEngineEvent) {
            abortOperation(event);
        }
    }

    private void abortOperation(FlowableEvent event) {
        String correlationId = getCorrelationId((FlowableEngineEvent) event);
        Operation operation = operationDao.findRequired(correlationId);
        LOGGER.info(MessageFormat.format(Messages.PROCESS_0_RELEASING_LOCK_FOR_MTA_1_IN_SPACE_2, operation.getProcessId(),
            operation.getMtaId(), operation.getSpaceId()));
        operation.setState(State.ABORTED);
        operation.setEndedAt(ZonedDateTime.now());
        operation.setAcquiredLock(false);
        operationDao.merge(operation);
        LOGGER.debug(MessageFormat.format(Messages.PROCESS_0_RELEASED_LOCK, operation.getProcessId()));
    }

    private String getCorrelationId(FlowableEngineEvent event) {
        String correlationId = FlowableEngineEventUtils.getVariable(event, Constants.VAR_CORRELATION_ID);
        return correlationId != null ? correlationId : event.getProcessInstanceId();
    }
}
