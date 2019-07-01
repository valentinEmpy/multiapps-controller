package com.sap.cloud.lm.sl.cf.process.util;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.impl.event.FlowableEntityExceptionEventImpl;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.runtime.Execution;
import org.flowable.job.service.impl.persistence.entity.AbstractJobEntityImpl;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;
import com.sap.cloud.lm.sl.cf.persistence.services.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.common.util.CommonUtil;

public class FlowableEntityExceptionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableEntityExceptionEventHandler.class);

    private ProgressMessageService progressMessageService;

    public FlowableEntityExceptionEventHandler(ProgressMessageService progressMessageService) {
        this.progressMessageService = progressMessageService;
    }

    public void handle(FlowableEntityExceptionEventImpl event) {
        try {
            logStacktrace(event.getCause());
            preserveAsProgressMessage(event);
        } catch (SLException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    private void logStacktrace(Throwable t) {
        String flowableExceptionStackTrace = ExceptionUtils.getStackTrace(t);
        LOGGER.error(flowableExceptionStackTrace);
    }

    private void preserveAsProgressMessage(FlowableEntityExceptionEventImpl event) {
        String flowableExceptionMessage = event.getCause()
            .getMessage();
        String processInstanceId = getProcessInstanceId(event);
        if (flowableExceptionMessage != null) {
            String errorMessage = MessageFormat.format(Messages.EXCEPTION_OCCURED_ERROR_MSG, flowableExceptionMessage);
            progressMessageService.add(new ProgressMessage(processInstanceId, getCurrentTaskId(event), ProgressMessageType.ERROR,
                errorMessage, new Timestamp(System.currentTimeMillis())));
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
            return CommonUtil.isNullOrEmpty(currentExecutionsForProcess) ? null : findCurrentExecution(currentExecutionsForProcess);
        } catch (Exception e) {
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