package com.sap.cloud.lm.sl.cf.process;

import java.text.MessageFormat;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.context.Context;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

public class AbortFailedProcessCommandFactory extends ConfigureJobRetryCommandFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbortFailedProcessCommandFactory.class);

    @Override
    public Command<Object> getCommand(String jobId, Throwable exception) {
        return new AbortFailedProcessCommand(jobId, getAbortReason(), super.getCommand(jobId, exception));
    }

    protected String getAbortReason() {
        return State.ABORTED.value();
    }

    protected static class AbortFailedProcessCommand extends FlowableCommand<Object> {

        private final String abortReason;

        public AbortFailedProcessCommand(String jobId, String abortReason, Command<Object> delegate) {
            super(jobId, delegate);
            this.abortReason = abortReason;
        }

        @Override
        public Object execute(CommandContext commandContext) {
            Object result = getDelegate().execute(commandContext);
            String jobProcessInstanceId = getJobProcessInstanceId();
            HistoricVariableInstance correlationId = getVariable(commandContext, jobProcessInstanceId, Constants.VAR_CORRELATION_ID);
            if (!jobProcessInstanceId.equals(correlationId.getValue())) {
                return result;
            }

            HistoricVariableInstance abortOnErrorVariable = getVariable(commandContext, jobProcessInstanceId,
                Constants.PARAM_ABORT_ON_ERROR);
            if (shouldAbortProcess(abortOnErrorVariable)) {
                abortProcess(commandContext, jobProcessInstanceId);
            }
            return result;
        }

        private boolean shouldAbortProcess(HistoricVariableInstance abortOnErrorVariable) {
            return abortOnErrorVariable != null && Boolean.TRUE.equals(abortOnErrorVariable.getValue());
        }

        private void abortProcess(CommandContext commandContext, String processId) {
            LOGGER.info(MessageFormat.format(Messages.PROCESS_WILL_BE_AUTO_ABORTED, processId));
            RuntimeService runtimeService = getRuntimeService(commandContext);
            runtimeService.deleteProcessInstance(processId, abortReason);
        }

        private RuntimeService getRuntimeService(CommandContext commandContext) {
            return Context.getProcessEngineConfiguration(commandContext)
                .getRuntimeService();
        }

    }

}
