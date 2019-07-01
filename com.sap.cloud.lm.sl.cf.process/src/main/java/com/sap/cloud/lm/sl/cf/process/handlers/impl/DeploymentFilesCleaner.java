package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEngineEventHandler;
import com.sap.cloud.lm.sl.common.util.Runnable;

public class DeploymentFilesCleaner extends FlowableEngineEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentFilesCleaner.class);
    private FileService fileService;
    private CloudControllerClientProvider clientProvider;

    public DeploymentFilesCleaner(FileService fileService, CloudControllerClientProvider clientProvider) {
        this.fileService = fileService;
        this.clientProvider = clientProvider;
    }

    @Override
    protected void handleInternal(FlowableEvent event) {
        ProcessDataCleaner processDataCleaner = getProcessDataCleaner(event);
        executeSafely(processDataCleaner::deleteDeploymentFiles);
    }

    protected ProcessDataCleaner getProcessDataCleaner(FlowableEvent event) {
        DelegateExecution context = new FlowableEngineEventToDelegateExecutionAdapter((FlowableEngineEvent) event);
        return new ProcessDataCleaner(fileService, clientProvider, context);
    }

    protected void executeSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) { // NOSONAR
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
