package com.sap.cloud.lm.sl.cf.process.listeners;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;

import javax.inject.Inject;

import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.dao.OperationDao;
import com.sap.cloud.lm.sl.cf.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableExecutionEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnEndProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.steps.StepsUtil;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

@Component("endProcessListener")
public class EndProcessListener extends AbstractProcessExecutionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(EndProcessListener.class);

    @Inject
    private OperationDao operationDao;

    @Inject
    @OnEndProcessQualifier
    private List<FlowableExecutionEventHandler> eventHandlers;

    @Override
    protected void notifyInternal(DelegateExecution context) throws FileStorageException {
        for (FlowableExecutionEventHandler eventHandler : eventHandlers) {
            eventHandler.handle(context);
        }
        setOperationInFinishedState(StepsUtil.getCorrelationId(context));
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