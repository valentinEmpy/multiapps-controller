package com.sap.cloud.lm.sl.cf.process.listeners;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.process.adapters.DelegateExecutionToFlowableProcessEventAdapter;
import com.sap.cloud.lm.sl.cf.process.events.FlowableProcessEventType;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEventHandlerChainBuilder;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.AnalyticsCollector;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.DeploymentFilesCleaner;
import com.sap.cloud.lm.sl.cf.process.handlers.impl.ProcessClientReleaser;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.cf.process.util.CollectedDataSender;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

@Component("endProcessListener")
public class EndProcessListener extends AbstractProcessExecutionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(EndProcessListener.class);

    @Inject
    private CollectedDataSender dataSender;

    @Inject
    @Named("fileService")
    private FileService fileService;

    @Inject
    private OperationDao operationDao;

    @Inject
    protected CloudControllerClientProvider clientProvider;

    @Inject
    private ApplicationConfiguration configuration;

    @Override
    protected void notifyInternal(DelegateExecution context) throws FileStorageException {
        FlowableEventHandler eventHandler = FlowableEventHandlerChainBuilder.of(new AnalyticsCollector(dataSender, configuration))
            .chainWith(new DeploymentFilesCleaner(fileService, clientProvider))
            .chainWith(new ProcessClientReleaser(fileService, clientProvider))
            .handleAllIf(this::isEndEvent)
            .build();
        eventHandler.handle(new DelegateExecutionToFlowableProcessEventAdapter(context));
        setOperationInFinishedState(StepsUtil.getCorrelationId(context));
    }

    private boolean isEndEvent(FlowableEvent event) {
        return event.getType() instanceof FlowableProcessEventType && event.getType()
            .equals(FlowableProcessEventType.START);
    }

    protected void setOperationInFinishedState(String processInstanceId) {
        Operation operation = operationDao.findRequired(processInstanceId);
        LOGGER.info(MessageFormat.format(Messages.PROCESS_0_RELEASING_LOCK_FOR_MTA_1_IN_SPACE_2, operation.getProcessId(),
            operation.getMtaId(), operation.getSpaceId()));
        operation.setState(State.FINISHED);
        operation.setEndedAt(ZonedDateTime.now());
        operation.setAcquiredLock(false);
        operationDao.merge(operation);
        LOGGER.debug(MessageFormat.format(Messages.PROCESS_0_RELEASED_LOCK, operation.getProcessId()));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}