package com.sap.cloud.lm.sl.cf.process.listeners;

import java.io.Serializable;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.FlowableProcessEngineEvent;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandlerChainBuilder;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.AnalyticsCollector;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.DeploymentFilesCleaner;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.OperationAborter;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.ProcessClientReleaser;
import com.sap.cloud.lm.sl.cf.process.util.CollectedDataSender;

@Component("abortProcessListener")
public class AbortProcessListener extends AbstractFlowableEventListener implements Serializable {

    private static final long serialVersionUID = 2L;

    @Inject
    private OperationDao operationDao;
    @Inject
    private CloudControllerClientProvider clientProvider;
    @Inject
    private FileService fileService;
    @Inject
    private ApplicationConfiguration configuration;
    @Inject
    private CollectedDataSender dataSender;

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        FlowableEventHandler eventHandler = FlowableEventHandlerChainBuilder.of(new OperationAborter(operationDao))
            .chainWith(new DeploymentFilesCleaner(fileService, clientProvider))
            .chainWith(new ProcessClientReleaser(fileService, clientProvider))
            .chainWith(new AnalyticsCollector(dataSender, configuration))
            .handleAllIf(this::isAborted)
            .build();
        eventHandler.handle(event);
    }

    protected boolean isAborted(FlowableEvent event) {
        if (!(event instanceof FlowableProcessEngineEvent)) {
            return false;
        }
        if (event.getType()
            .equals(FlowableEngineEventType.PROCESS_CANCELLED)) {
            return true;
        }
        return event.getType()
            .equals(FlowableEngineEventType.ENTITY_DELETED) && hasCorrectEntityType((FlowableProcessEngineEvent) event);
    }

    private boolean hasCorrectEntityType(FlowableProcessEngineEvent processEngineEvent) {
        FlowableEntityEvent flowableEntityEvent = (FlowableEntityEvent) processEngineEvent;
        if (!(flowableEntityEvent.getEntity() instanceof ExecutionEntity)) {
            return false;
        }

        ExecutionEntity executionEntity = (ExecutionEntity) flowableEntityEvent.getEntity();
        return executionEntity.isProcessInstanceType() && Constants.PROCESS_ABORTED.equals(executionEntity.getDeleteReason());
    }
}
