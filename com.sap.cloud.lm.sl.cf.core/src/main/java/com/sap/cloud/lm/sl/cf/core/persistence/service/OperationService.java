package com.sap.cloud.lm.sl.cf.core.persistence.service;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.OperationDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.OperationFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.OperationQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.OperationQueryImpl;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Component
public class OperationService extends PersistenceService<Operation, OperationDto, String> {

    @Inject
    private OperationFactory operationFactory;

    @Inject
    public OperationService(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, OperationDto.class);
    }

    public OperationQuery createQuery() {
        return new OperationQueryImpl(createEntityManager(), operationFactory);
    }

    @Override
    protected PersistenceObjectFactory<Operation, OperationDto> getPersistenceObjectFactory() {
        return operationFactory;
    }

    @Override
    protected void onEntityNotFound(String processId) {
        throw new NotFoundException(Messages.OPERATION_NOT_FOUND, processId);
    }

    @Override
    protected void onEntityConflict(OperationDto dto, Throwable t) {
        String processId = dto.getPrimaryKey();
        throw (ConflictException) new ConflictException(Messages.OPERATION_ALREADY_EXISTS, processId).initCause(t);
    }

}
