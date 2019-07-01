package com.sap.cloud.lm.sl.cf.process.listeners;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.FlowableProcessEngineEvent;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnAbortProcessQualifier;

@Component("abortProcessListener")
public class AbortProcessListener extends AbstractFlowableEventListener implements Serializable {

    private static final long serialVersionUID = 2L;

    @Inject
    @OnAbortProcessQualifier
    private List<FlowableEventHandler> eventHandlers;

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (!isEventValid(event)) {
            return;
        }

        FlowableEngineEvent engineEvent = (FlowableEngineEvent) event;
        for (FlowableEventHandler eventHandler : eventHandlers) {
            eventHandler.handle(engineEvent);
        }
    }

    private boolean isEventValid(FlowableEvent event) {
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
