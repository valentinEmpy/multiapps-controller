package com.sap.cloud.lm.sl.cf.process.events;

import org.flowable.common.engine.api.delegate.event.FlowableEventType;

public enum FlowableProcessEventType implements FlowableEventType {

    START,

    END;

    public static FlowableProcessEventType lookup(String eventName) {
        for (FlowableProcessEventType processEvent : FlowableProcessEventType.values()) {
            if (processEvent.name()
                .equalsIgnoreCase(eventName)) {
                return processEvent;
            }
        }
        return null;
    }
}
