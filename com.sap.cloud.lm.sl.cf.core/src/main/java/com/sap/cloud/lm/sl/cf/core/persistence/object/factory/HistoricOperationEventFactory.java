package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableHistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.HistoricOperationEventDto;

@Component
public class HistoricOperationEventFactory extends AbstractPersistenceObjectFactory<HistoricOperationEvent, HistoricOperationEventDto> {

    @Override
    protected HistoricOperationEvent fromNonNullDto(HistoricOperationEventDto dto) {
        return ImmutableHistoricOperationEvent.builder()
            .id(dto.getPrimaryKey())
            .processId(dto.getProcessId())
            .type(getType(dto.getType()))
            .timestamp(dto.getTimestamp())
            .build();
    }

    private EventType getType(String type) {
        return EventType.valueOf(type);
    }

    @Override
    protected HistoricOperationEventDto nonNullObjectToDto(HistoricOperationEvent historicOperationEvent) {
        long id = historicOperationEvent.getId();
        String processId = historicOperationEvent.getProcessId();
        String type = historicOperationEvent.getType()
            .name();
        Date timestamp = historicOperationEvent.getTimestamp();
        return new HistoricOperationEventDto(id, processId, type, timestamp);
    }

}
