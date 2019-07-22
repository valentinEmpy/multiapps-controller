package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Date;

public class HistoricOperationEvent {

    private long id;
    private String processId;
    private EventType type;
    private Date timestamp;

    public HistoricOperationEvent() {
    }

    public HistoricOperationEvent(long id, String processId, EventType type, Date timestamp) {
        this.id = id;
        this.processId = processId;
        this.type = type;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public enum EventType {
        STARTED, FINISHED, FAILED_BY_CONTENT_ERROR, FAILED_BY_INFRASTRUCTURE_ERROR, RETRIED, ABORTED
    }

    public static class Builder {

        private long id;
        private String processId;
        private EventType type;
        private Date timestamp;

        public Builder(String processId, EventType type) {
            this.processId = processId;
            this.type = type;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public HistoricOperationEvent build() {
            if (timestamp == null) {
                timestamp = new Date(System.currentTimeMillis());
            }
            return new HistoricOperationEvent(id, processId, type, timestamp);
        }
    }

}
