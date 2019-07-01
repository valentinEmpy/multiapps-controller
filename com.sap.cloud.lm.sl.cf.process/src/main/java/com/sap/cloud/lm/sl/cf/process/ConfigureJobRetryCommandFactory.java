package com.sap.cloud.lm.sl.cf.process;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.util.CommandContextUtil;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.common.ContentException;

/**
 * Sets number of retries on failed Activiti job. By default Activiti will retry failed jobs three times.
 */
public class ConfigureJobRetryCommandFactory extends DefaultFailedJobCommandFactory {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigureJobRetryCommandFactory.class);

    @Override
    public Command<Object> getCommand(String jobId, Throwable exception) {
        return new ConfigureJobRetryCommand(jobId, exception, super.getCommand(jobId, exception));
    }

    public static class ConfigureJobRetryCommand extends FlowableCommand<Object> {

        private static final int MAX_RETRIES = 5;
        private static final int DEFAULT_RETRIES = 3;
        private static final int NO_RETRIES = 0;
        private Throwable exception;

        public ConfigureJobRetryCommand(String jobId, Throwable exception, Command<Object> delegate) {
            super(jobId, delegate);
            this.exception = exception;
        }

        @Override
        public Object execute(CommandContext commandContext) {
            JobEntity job = getJobEntity(commandContext);
            prepareForRetry(job, commandContext);
            return getDelegate().execute(commandContext);
        }

        private JobEntity getJobEntity(CommandContext commandContext) {
            return CommandContextUtil.getJobServiceConfiguration(commandContext)
                .getJobService()
                .findJobById(getJobId());
        }

        private void prepareForRetry(JobEntity job, CommandContext commandContext) {
            if (isFirstCommandExecution(job)) {
                String correlationId = (String) getVariable(commandContext, job.getProcessInstanceId(), Constants.VAR_CORRELATION_ID)
                    .getValue();
                job.setRetries(getRetries(commandContext, correlationId));
            }
            job.setLockOwner(null);
            job.setLockExpirationTime(null);
        }

        private boolean isFirstCommandExecution(JobEntity job) {
            return job.getExceptionMessage() == null;
        }

        private int getRetries(CommandContext commandContext, String correlationId) {
            if (isContentException()) {
                return NO_RETRIES;
            }

            HistoricVariableInstance retryCountVariable = getVariable(commandContext, correlationId, Constants.VAR_RETRIES);
            if (retryCountVariable == null) {
                return DEFAULT_RETRIES + 1;
            }
            return getRetries(retryCountVariable) + 1;
        }

        private boolean isContentException() {
            return exception != null && exception.getCause() instanceof ContentException;
        }

        private int getRetries(HistoricVariableInstance retryCountVariable) {
            try {
                Integer retries = Integer.parseInt((String) retryCountVariable.getValue());
                return retries > MAX_RETRIES ? MAX_RETRIES : retries;
            } catch (NumberFormatException e) {
                return DEFAULT_RETRIES;
            }
        }
    }
}
