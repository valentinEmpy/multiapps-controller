package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import java.util.Date;

import javax.inject.Named;

import com.sap.cloud.lm.sl.cf.core.persistence.dto.ProgressMessageDto;
import com.sap.cloud.lm.sl.cf.persistence.model.ImmutableProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;

@Named
public class ProgressMessageFactory implements PersistenceObjectFactory<ProgressMessage, ProgressMessageDto> {

    @Override
    public ProgressMessage fromDto(ProgressMessageDto dto) {
        return ImmutableProgressMessage.builder()
                                       .id(dto.getPrimaryKey())
                                       .processId(dto.getProcessId())
                                       .taskId(dto.getTaskId())
                                       .type(getParsedType(dto.getType()))
                                       .text(dto.getText())
                                       .timestamp(dto.getTimestamp())
                                       .build();
    }

    private ProgressMessageType getParsedType(String type) {
        return type == null ? null : ProgressMessageType.valueOf(type);
    }

    @Override
    public ProgressMessageDto toDto(ProgressMessage progressMessage) {
        long id = progressMessage.getId();
        String processId = progressMessage.getProcessId();
        String taskId = progressMessage.getTaskId();
        String type = progressMessage.getType() != null ? progressMessage.getType()
                                                                         .name()
            : null;
        String text = progressMessage.getText();
        Date timestamp = progressMessage.getTimestamp();
        return new ProgressMessageDto(id, processId, taskId, type, text, timestamp);
    }

}