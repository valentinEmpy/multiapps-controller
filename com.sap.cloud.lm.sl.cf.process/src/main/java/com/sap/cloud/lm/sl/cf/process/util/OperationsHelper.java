package com.sap.cloud.lm.sl.cf.process.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;
import com.sap.cloud.lm.sl.cf.core.persistence.service.HistoricOperationEventService;
import com.sap.cloud.lm.sl.cf.core.persistence.service.OperationService;
import com.sap.cloud.lm.sl.cf.process.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.cf.process.metadata.ProcessTypeToOperationMetadataMapper;
import com.sap.cloud.lm.sl.cf.web.api.model.ErrorType;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.ProcessType;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

@Component
public class OperationsHelper {

    @Inject
    private OperationService operationService;

    @Inject
    private ProcessTypeToOperationMetadataMapper metadataMapper;

    @Inject
    private FlowableFacade flowableFacade;

    @Inject
    private HistoricOperationEventService historicOperationEventService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationsHelper.class);

    public String getProcessDefinitionKey(Operation operation) {
        return metadataMapper.getDiagramId(operation.getProcessType());
    }

    public List<Operation> findOperations(List<Operation> operations, List<State> statusList) {
        addOngoingOperationsState(operations);
        return filterBasedOnStates(operations, statusList);
    }

    private void addOngoingOperationsState(List<Operation> existingOngoingOperations) {
        for (Operation ongoingOperation : existingOngoingOperations) {
            addState(ongoingOperation);
        }
    }

    public void addState(Operation ongoingOperation) {
        ongoingOperation.setState(getOngoingOperationState(ongoingOperation));
    }

    protected State getOngoingOperationState(Operation ongoingOperation) {
        if (ongoingOperation.getState() != null) {
            return ongoingOperation.getState();
        }
        State state = computeState(ongoingOperation);
        // Fixes bug XSBUG-2035: Inconsistency in 'operation', 'act_hi_procinst' and 'act_ru_execution' tables
        if (ongoingOperation.hasAcquiredLock() && (state.equals(State.ABORTED) || state.equals(State.FINISHED))) {
            ongoingOperation.acquiredLock(false);
            ongoingOperation.setState(state);
            operationService.update(ongoingOperation.getProcessId(), ongoingOperation);
        }
        return state;
    }

    private List<Operation> filterBasedOnStates(List<Operation> operations, List<State> statusList) {
        if (CollectionUtils.isEmpty(statusList)) {
            return operations;
        }
        return operations.stream()
            .filter(operation -> statusList.contains(operation.getState()))
            .collect(Collectors.toList());
    }

    public List<String> getAllProcessDefinitionKeys(Operation operation) {
        List<String> processDefinitionKeys = new ArrayList<>();
        ProcessType processType = operation.getProcessType();
        processDefinitionKeys.add(metadataMapper.getDiagramId(processType));
        processDefinitionKeys.addAll(metadataMapper.getPreviousDiagramIds(processType));
        return processDefinitionKeys;
    }

    public void addErrorType(Operation operation) {
        if (operation.getState() == State.ERROR) {
            operation.setErrorType(getErrorType(operation));
        }
    }

    public ErrorType getErrorType(Operation operation) {
        List<HistoricOperationEvent> historicEvents = historicOperationEventService.createQuery()
            .processId(operation.getProcessId())
            .list();
        EventType historicErrorType = null;
        for (HistoricOperationEvent historicEvent : historicEvents) {
            if (historicEvent.getType() == EventType.RETRIED) {
                historicErrorType = null;
            }
            if ((historicEvent.getType() == EventType.FAILED_BY_CONTENT_ERROR
                || historicEvent.getType() == EventType.FAILED_BY_INFRASTRUCTURE_ERROR)
                && historicErrorType != EventType.FAILED_BY_INFRASTRUCTURE_ERROR) {
                historicErrorType = historicEvent.getType();
            }
        }
        return toErrorType(historicErrorType);
    }

    public ErrorType toErrorType(EventType historicType) {
        if (historicType == EventType.FAILED_BY_CONTENT_ERROR) {
            return ErrorType.CONTENT;
        }
        if (historicType == EventType.FAILED_BY_INFRASTRUCTURE_ERROR) {
            return ErrorType.INFRASTRUCTURE;
        }
        return null;
    }

    public State computeState(Operation ongoingOperation) {
        LOGGER.debug(MessageFormat.format(Messages.COMPUTING_STATE_OF_OPERATION, ongoingOperation.getProcessType(),
            ongoingOperation.getProcessId()));
        return flowableFacade.getProcessInstanceState(ongoingOperation.getProcessId());
    }

}
