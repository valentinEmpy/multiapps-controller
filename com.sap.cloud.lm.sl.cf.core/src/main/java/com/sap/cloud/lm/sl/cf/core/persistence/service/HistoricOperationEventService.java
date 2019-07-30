package com.sap.cloud.lm.sl.cf.core.persistence.service;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.HistoricOperationEventDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.HistoricOperationEventFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.HistoricOperationEventQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.HistoricOperationEventQueryImpl;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Component
public class HistoricOperationEventService extends PersistenceService<HistoricOperationEvent, HistoricOperationEventDto, Long> {

    @Inject
    private HistoricOperationEventFactory historicOperationEventFactory;

    @Inject
    public HistoricOperationEventService(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, HistoricOperationEventDto.class);
    }
    
    public HistoricOperationEventQuery createQuery() {
        return new HistoricOperationEventQueryImpl(createEntityManager(), historicOperationEventFactory);
    }

    @Override
    protected PersistenceObjectFactory<HistoricOperationEvent, HistoricOperationEventDto> getPersistenceObjectFactory() {
        return historicOperationEventFactory;
    }

    @Override
    protected void onEntityConflict(HistoricOperationEventDto dto, Throwable t) {
        throw (ConflictException) new ConflictException(Messages.HISTORIC_OPERATION_EVENT_ALREADY_EXISTS, dto.getProcessId(),
            dto.getPrimaryKey()).initCause(t);
    }

    @Override
    protected void onEntityNotFound(Long id) {
        throw new NotFoundException(Messages.HISTORIC_OPERATION_EVENT_NOT_FOUND, id);
    }

}
