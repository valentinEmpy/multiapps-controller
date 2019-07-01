package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import org.flowable.engine.delegate.DelegateExecution;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.cf.process.util.FileSweeper;

public class ProcessDataCleaner {

    private FileService fileService;
    private CloudControllerClientProvider clientProvider;
    private DelegateExecution context;

    public ProcessDataCleaner(FileService fileService, CloudControllerClientProvider clientProvider, DelegateExecution context) {
        this.fileService = fileService;
        this.clientProvider = clientProvider;
        this.context = context;
    }

    public void removeClientForProcess() {
        String user = getCurrentUser();
        String space = StepsUtil.getSpace(context);
        String org = StepsUtil.getOrg(context);
        String spaceID = StepsUtil.getSpaceId(context);

        clientProvider.releaseClient(user, org, space);
        clientProvider.releaseClient(user, spaceID);
    }

    protected String getCurrentUser() {
        String user = (String) context.getVariable(Constants.VAR_USER);
        if (user == null) {
            user = (String) context.getVariable(Constants.PARAM_INITIATOR);
        }
        return user;
    }

    public void deleteDeploymentFiles() throws FileStorageException {
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
}
