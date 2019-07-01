package com.sap.cloud.lm.sl.cf.process;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.HistoryService;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.variable.api.history.HistoricVariableInstance;

public abstract class FlowableCommand<T> implements Command<T> {

    private String jobId;
    private Command<T> delegate;

    public FlowableCommand(String jobId, Command<T> delegate) {
        this.jobId = jobId;
        this.delegate = delegate;
    }

    protected String getJobId() {
        return jobId;
    }

    protected Command<T> getDelegate() {
        return delegate;
    }

    protected String getJobProcessInstanceId() {
        JobEntity job = CommandContextUtil.getJobServiceConfiguration()
            .getJobEntityManager()
            .findById(jobId);
        return job.getProcessInstanceId();
    }

    protected HistoricVariableInstance getVariable(CommandContext commandContext, String processId, String variableName) {
        return getHistoryService(commandContext).createHistoricVariableInstanceQuery()
            .processInstanceId(processId)
            .variableName(variableName)
            .singleResult();
    }

    protected HistoryService getHistoryService(CommandContext commandContext) {
        return Context.getProcessEngineConfiguration(commandContext)
            .getHistoryService();
    }
}
