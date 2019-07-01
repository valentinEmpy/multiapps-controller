package com.sap.cloud.lm.sl.cf.process.events;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;

public class FlowableProcessEvent implements FlowableEvent {

    private FlowableProcessEventType type;

    public FlowableProcessEvent(FlowableProcessEventType type) {
        this.type = type;
    }

    @Override
    public FlowableEventType getType() {
        return type;
    }

}
