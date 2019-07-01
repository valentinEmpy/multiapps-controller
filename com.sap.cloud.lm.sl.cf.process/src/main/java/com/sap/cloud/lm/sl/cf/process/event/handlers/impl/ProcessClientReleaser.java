package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableExecutionEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnAbortProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnEndProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnErrorJobQualifier;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.common.util.Runnable;

@Component
@OnAbortProcessQualifier
@OnErrorJobQualifier
@OnEndProcessQualifier
public class ProcessClientReleaser implements FlowableEventHandler, FlowableExecutionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessClientReleaser.class);
    private CloudControllerClientProvider clientProvider;

    @Inject
    public ProcessClientReleaser(CloudControllerClientProvider clientProvider) {
        this.clientProvider = clientProvider;
    }

    @Override
    public void handle(DelegateExecution context) {
        removeClientSafely(context);
    }

    @Override
    public void handle(FlowableEvent event) {
        if (event instanceof FlowableEngineEvent) {
            removeClientSafely(new FlowableEngineEventToDelegateExecutionAdapter((FlowableEngineEvent) event));
        }
    }

    private void removeClientSafely(DelegateExecution context) {
        executeSafely(() -> removeClientForProcess(context));
    }

    public void removeClientForProcess(DelegateExecution context) {
        String user = getCurrentUser(context);
        String space = StepsUtil.getSpace(context);
        String org = StepsUtil.getOrg(context);
        String spaceID = StepsUtil.getSpaceId(context);

        clientProvider.releaseClient(user, org, space);
        clientProvider.releaseClient(user, spaceID);
    }

    protected String getCurrentUser(DelegateExecution context) {
        String user = (String) context.getVariable(Constants.VAR_USER);
        if (user == null) {
            user = (String) context.getVariable(Constants.PARAM_INITIATOR);
        }
        return user;
    }

    protected void executeSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) { // NOSONAR
            LOGGER.warn(e.getMessage(), e);
        }
    }

}
