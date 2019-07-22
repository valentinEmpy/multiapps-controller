package com.sap.cloud.lm.sl.cf.process.flowable;

import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.core.dao.HistoricOperationEventDao;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;

@Named
public class RetryProcessAction extends ProcessAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryProcessAction.class);

    public static final String ACTION_ID_RETRY = "retry";

    private HistoricOperationEventDao historicOperationEventDao;

    @Inject
    public RetryProcessAction(FlowableFacade flowableFacade, List<AdditionalProcessAction> additionalProcessActions,
        HistoricOperationEventDao historicOperationEventDao) {
        super(flowableFacade, additionalProcessActions);
        this.historicOperationEventDao = historicOperationEventDao;
    }

    @Override
    protected void executeActualProcessAction(String userId, String superProcessInstanceId) {
        List<String> subProcessIds = getActiveExecutionIds(superProcessInstanceId);
        ListIterator<String> subProcessesIdsIterator = subProcessIds.listIterator(subProcessIds.size());

        updateUser(userId, superProcessInstanceId);
        while (subProcessesIdsIterator.hasPrevious()) {
            String subProcessId = subProcessesIdsIterator.previous();
            retryProcess(userId, subProcessId);
        }
        addHistoricOperationEvent(superProcessInstanceId, EventType.RETRIED);
    }

    private void retryProcess(String userId, String subProcessId) {
        try {
            flowableFacade.executeJob(userId, subProcessId);
        } catch (RuntimeException e) {
            // Consider the retry as successful. The execution error could be later obtained through
            // the getError() method.
            LOGGER.error(Messages.FLOWABLE_JOB_RETRY_FAILED, e);
        }
    }

    protected void addHistoricOperationEvent(String operationId, EventType type) {
        HistoricOperationEvent historicOperationEvent = new HistoricOperationEvent.Builder(operationId, type).build();
        historicOperationEventDao.add(historicOperationEvent);
    }

    @Override
    public String getActionId() {
        return ACTION_ID_RETRY;
    }

}
