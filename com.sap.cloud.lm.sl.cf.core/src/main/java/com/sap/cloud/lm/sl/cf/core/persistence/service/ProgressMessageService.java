package com.sap.cloud.lm.sl.cf.core.persistence.service;

import java.util.Date;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ProgressMessageDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ProgressMessageFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ProgressMessageQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.ProgressMessageQueryImpl;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Component
public class ProgressMessageService extends PersistenceService<ProgressMessage, ProgressMessageDto, Long> {

    @Inject
    protected ProgressMessageFactory progressMessageFactory;

    @Inject
    public ProgressMessageService(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    public ProgressMessageQuery createQuery() {
        return new ProgressMessageQueryImpl(createEntityManager(), progressMessageFactory);
    }

    @Override
    protected ProgressMessageDto merge(ProgressMessageDto existingProgressMessage, ProgressMessageDto newProgressMessage) {
        super.merge(existingProgressMessage, newProgressMessage);
        String processId = ObjectUtils.firstNonNull(newProgressMessage.getProcessId(), existingProgressMessage.getProcessId());
        String taskId = ObjectUtils.firstNonNull(newProgressMessage.getTaskId(), existingProgressMessage.getTaskId());
        String type = ObjectUtils.firstNonNull(newProgressMessage.getType(), existingProgressMessage.getType());
        String text = ObjectUtils.firstNonNull(newProgressMessage.getText(), existingProgressMessage.getText());
        Date timestamp = ObjectUtils.firstNonNull(newProgressMessage.getTimestamp(), existingProgressMessage.getTimestamp());
        return getProgressMessageDto(newProgressMessage.getPrimaryKey(), processId, taskId, type, text, timestamp);
    }

    protected ProgressMessageDto getProgressMessageDto(long id, String processId, String taskId, String type, String text, Date timestamp) {
        return new ProgressMessageDto(id, processId, taskId, type, text, timestamp);
    }

    @Override
    protected PersistenceObjectFactory<ProgressMessage, ProgressMessageDto> getPersistenceObjectFactory() {
        return progressMessageFactory;
    }

    @Override
    protected void onEntityConflict(ProgressMessageDto progressMessage, Throwable t) {
        throw (ConflictException) new ConflictException(Messages.PROGRESS_MESSAGE_ALREADY_EXISTS,
                                                        progressMessage.getProcessId(),
                                                        progressMessage.getPrimaryKey()).initCause(t);
    }

    @Override
    protected void onEntityNotFound(Long id) {
        throw new NotFoundException(Messages.PROGRESS_MESSAGE_NOT_FOUND, id);
    }

}