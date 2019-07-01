package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableExecutionEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnAbortProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnEndProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.cf.process.util.FileSweeper;
import com.sap.cloud.lm.sl.common.util.Runnable;

@Component
@OnAbortProcessQualifier
@OnEndProcessQualifier
public class DeploymentFilesCleaner implements FlowableEventHandler, FlowableExecutionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentFilesCleaner.class);
    private FileService fileService;

    @Inject
    public DeploymentFilesCleaner(@Named("fileService") FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void handle(DelegateExecution context) {
        deleteDeploymentFilesSafely(context);
    }

    @Override
    public void handle(FlowableEvent event) {
        if (event instanceof FlowableEngineEvent) {
            DelegateExecution context = new FlowableEngineEventToDelegateExecutionAdapter((FlowableEngineEvent) event);
            deleteDeploymentFilesSafely(context);
        }
    }

    public void deleteDeploymentFilesSafely(DelegateExecution context) {
        executeSafely(() -> deleteDeploymentFiles(context));
    }

    public void deleteDeploymentFiles(DelegateExecution context) throws FileStorageException {
        if (shouldKeepFiles((Boolean) context.getVariable(Constants.PARAM_KEEP_FILES))) {
            return;
        }

        String extensionDescriptorFileIds = (String) context.getVariable(Constants.PARAM_EXT_DESCRIPTOR_FILE_ID);
        String appArchiveFileIds = (String) context.getVariable(Constants.PARAM_APP_ARCHIVE_ID);

        FileSweeper fileSweeper = new FileSweeper(StepsUtil.getSpaceId(context), fileService);
        fileSweeper.sweep(extensionDescriptorFileIds);
        fileSweeper.sweep(appArchiveFileIds);
    }

    private boolean shouldKeepFiles(Boolean keepFiles) {
        return keepFiles != null && keepFiles;
    }

    protected void executeSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) { // NOSONAR
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
