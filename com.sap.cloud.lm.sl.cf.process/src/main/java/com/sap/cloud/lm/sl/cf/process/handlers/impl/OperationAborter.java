package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEngineEventHandler;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

public class OperationAborter extends FlowableEngineEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationAborter.class);
    private OperationDao operationDao;

    public OperationAborter(OperationDao operationDao) {
        this.operationDao = operationDao;
    }

    @Override
    protected void handleInternal(FlowableEvent event) {
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
        String correlationId = getVariable(event, Constants.VAR_CORRELATION_ID);
        return correlationId != null ? correlationId : event.getProcessInstanceId();
    }

}
