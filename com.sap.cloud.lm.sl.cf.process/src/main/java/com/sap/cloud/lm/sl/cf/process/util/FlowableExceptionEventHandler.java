package com.sap.cloud.lm.sl.cf.process.util;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableExceptionEvent;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.runtime.Execution;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.core.dao.HistoricOperationEventDao;
import com.sap.cloud.lm.sl.cf.core.dao.ProgressMessageDao;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.common.util.CommonUtil;

public class FlowableExceptionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableExceptionEventHandler.class);

    private ProgressMessageDao progressMessageDao;
    private HistoricOperationEventDao historicOperationEventDao;

    public FlowableExceptionEventHandler(ProgressMessageDao progressMessageDao, HistoricOperationEventDao historicOperationEventDao) {
        this.progressMessageDao = progressMessageDao;
        this.historicOperationEventDao = historicOperationEventDao;
    }

    public void handle(FlowableEvent event) {
        if (!(event instanceof FlowableExceptionEvent) && !(event instanceof FlowableEngineEvent)) {
            return;
        }

        FlowableExceptionEvent flowableExceptionEvent = (FlowableExceptionEvent) event;
        Throwable cause = flowableExceptionEvent.getCause();
        String flowableExceptionStackTrace = ExceptionUtils.getStackTrace(cause);
        LOGGER.error(flowableExceptionStackTrace);

        if (cause.getMessage() == null) {
            return;
        }

        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        addHistoricOperationEvent(getProcessInstanceId(engineEvent), cause);
        try {
            tryToPreserveFlowableException(event, cause.getMessage());
        } catch (SLException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    protected void addHistoricOperationEvent(String operationId, Throwable cause) {
        EventType type = (cause instanceof ContentException) ? EventType.FAILED_BY_CONTENT_ERROR : EventType.FAILED_BY_INFRASTRUCTURE_ERROR;
        HistoricOperationEvent historicOperationEvent = new HistoricOperationEvent.Builder(operationId, type).build();
        historicOperationEventDao.add(historicOperationEvent);
    }

    private void tryToPreserveFlowableException(FlowableEvent event, String flowableExceptionMessage) {
        FlowableEngineEvent flowableEngineEvent = (FlowableEngineEvent) event;

        String taskId = getCurrentTaskId(flowableEngineEvent);
        String errorMessage = MessageFormat.format(Messages.UNEXPECTED_ERROR, flowableExceptionMessage);
        String processInstanceId = getProcessInstanceId(flowableEngineEvent);
        List<ProgressMessage> progressMessages = progressMessageDao.find(processInstanceId);
        Optional<ProgressMessage> errorProgressMessage = progressMessages.stream()
            .filter(message -> message.getType() == ProgressMessageType.ERROR)
            .findAny();

        if (!errorProgressMessage.isPresent()) {
            progressMessageDao.add(new ProgressMessage(processInstanceId, taskId, ProgressMessageType.ERROR, errorMessage,
                new Timestamp(System.currentTimeMillis())));
        }
    }

    private String getCurrentTaskId(FlowableEngineEvent flowableEngineEvent) {
        Execution currentExecutionForProces = findCurrentExecution(flowableEngineEvent);

        return currentExecutionForProces != null ? currentExecutionForProces.getActivityId()
            : getVariable(flowableEngineEvent, Constants.TASK_ID);
    }

    private Execution findCurrentExecution(FlowableEngineEvent flowableEngineEvent) {
        try {
            // This is needed because when there are parallel CallActivity, the query will return multiple results for just one Execution
            List<Execution> currentExecutionsForProcess = Context.getProcessEngineConfiguration()
                .getRuntimeService()
                .createExecutionQuery()
                .executionId(flowableEngineEvent.getExecutionId())
                .processInstanceId(flowableEngineEvent.getProcessInstanceId())
                .list();

            // Based on the above comment, one of the executions will have null activityId(because it will be the monitoring one) and thus
            // should be excluded from the list of executions
            Execution currentExecutionForProces = CommonUtil.isNullOrEmpty(currentExecutionsForProcess) ? null
                : findCurrentExecution(currentExecutionsForProcess);
            return currentExecutionForProces;
        } catch (Throwable e) {
            return null;
        }
    }

    private Execution findCurrentExecution(List<Execution> currentExecutionsForProcess) {
        return currentExecutionsForProcess.stream()
            .filter(execution -> execution.getActivityId() != null)
            .findFirst()
            .orElse(null);
    }

    private String getProcessInstanceId(FlowableEvent event) {
        return getVariable((FlowableEngineEvent) event, Constants.VAR_CORRELATION_ID);
    }

    private String getVariable(FlowableEngineEvent event, String variableName) {
        VariableInstance variableInstance = Context.getProcessEngineConfiguration()
            .getRuntimeService()
            .getVariableInstance(event.getExecutionId(), variableName);

        if (variableInstance == null) {
            return getVariableFromHistoryService(event, variableName);
        }

        return variableInstance.getTextValue();
    }

    private String getVariableFromHistoryService(FlowableEngineEvent event, String variableName) {
        HistoricVariableInstance historicVariableInstance = Context.getProcessEngineConfiguration()
            .getHistoryService()
            .createHistoricVariableInstanceQuery()
            .executionId(event.getExecutionId())
            .variableName(variableName)
            .singleResult();

        if (historicVariableInstance == null) {
            return null;
        }

        return (String) historicVariableInstance.getValue();
    }
}
