package com.sap.cloud.lm.sl.cf.process.adapters;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.engine.delegate.DelegateExecution;

import com.sap.cloud.lm.sl.cf.process.events.FlowableProcessEvent;
import com.sap.cloud.lm.sl.cf.process.events.FlowableProcessEventType;

public class DelegateExecutionToFlowableProcessEventAdapter extends FlowableProcessEvent implements FlowableEngineEvent {

    private DelegateExecution context;

    public DelegateExecutionToFlowableProcessEventAdapter(DelegateExecution context) {
        super(FlowableProcessEventType.lookup(context.getEventName()));
        this.context = context;
    }

    @Override
    public String getExecutionId() {
        return context.getId();
    }

    @Override
    public String getProcessInstanceId() {
        return context.getProcessInstanceId();
    }

    @Override
    public String getProcessDefinitionId() {
        return context.getProcessDefinitionId();
    }
}
