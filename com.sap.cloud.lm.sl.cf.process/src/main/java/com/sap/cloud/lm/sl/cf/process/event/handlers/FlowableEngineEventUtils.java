package com.sap.cloud.lm.sl.cf.process.event.handlers;

import java.util.List;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.runtime.Execution;

import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.common.util.CommonUtil;

public class FlowableEngineEventUtils {

    private FlowableEngineEventUtils() {
    }

    public static String getTaskId(FlowableEngineEvent engineEvent) {
        Execution currentExecutionForProces = findCurrentExecution(engineEvent);
        return currentExecutionForProces != null ? currentExecutionForProces.getActivityId() : getVariable(engineEvent, Constants.TASK_ID);
    }

    public static String getVariable(FlowableEngineEvent event, String variableName) {
        return (String) new FlowableEngineEventToDelegateExecutionAdapter(event).getVariable(variableName);
    }

    public static Execution findCurrentExecution(FlowableEngineEvent engineEvent) {
        try {
            // This is needed because when there are parallel CallActivity, the query will return multiple results for just one Execution
            List<Execution> currentExecutionsForProcess = Context.getProcessEngineConfiguration()
                .getRuntimeService()
                .createExecutionQuery()
                .executionId(engineEvent.getExecutionId())
                .processInstanceId(engineEvent.getProcessInstanceId())
                .list();

            // Based on the above comment, one of the executions will have null activityId(because it will be the monitoring one) and thus
            // should be excluded from the list of executions
            return CommonUtil.isNullOrEmpty(currentExecutionsForProcess) ? null : findCurrentExecution(currentExecutionsForProcess);
        } catch (Exception e) {
            return null;
        }
    }

    private static Execution findCurrentExecution(List<Execution> currentExecutionsForProcess) {
        return currentExecutionsForProcess.stream()
            .filter(execution -> execution.getActivityId() != null)
            .findFirst()
            .orElse(null);
    }
}
