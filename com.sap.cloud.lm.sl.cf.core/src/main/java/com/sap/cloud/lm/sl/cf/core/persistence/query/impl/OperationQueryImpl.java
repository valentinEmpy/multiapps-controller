package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.sap.cloud.lm.sl.cf.core.persistence.dto.OperationDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.OperationFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.OperationQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeInCollectionRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeLiteralRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeRestriction;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.ProcessType;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

public class OperationQueryImpl extends AbstractQueryImpl<Operation, OperationQuery> implements OperationQuery {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private Map<String, AbstractAttributeRestriction> attributeRestrictions = new HashMap<>();
    private OperationFactory operationFactory;

    public OperationQueryImpl(EntityManager entityManager, OperationFactory operationFactory) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.operationFactory = operationFactory;
    }

    @Override
    public OperationQuery processId(String processId) {
        attributeRestrictions.put(OperationDto.AttributeNames.PROCESS_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, processId));
        return this;
    }

    private void addEqualsRestriction(String attributeName, Object value) {
        attributeRestrictions.put(attributeName, AttributeLiteralRestriction.of(criteriaBuilder::equal, value));
    }

    @Override
    public OperationQuery processType(ProcessType processType) {
        attributeRestrictions.put(OperationDto.AttributeNames.PROCESS_TYPE,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, processType));
        addEqualsRestriction(OperationDto.AttributeNames.PROCESS_TYPE, processType);
        return this;
    }

    @Override
    public OperationQuery user(String user) {
        attributeRestrictions.put(OperationDto.AttributeNames.USER, AttributeLiteralRestriction.of(criteriaBuilder::equal, user));
        addEqualsRestriction(OperationDto.AttributeNames.USER, user);
        return this;
    }

    @Override
    public OperationQuery spaceId(String spaceId) {
        attributeRestrictions.put(OperationDto.AttributeNames.SPACE_ID, AttributeLiteralRestriction.of(criteriaBuilder::equal, spaceId));
        addEqualsRestriction(OperationDto.AttributeNames.SPACE_ID, spaceId);
        return this;
    }

    @Override
    public OperationQuery mtaId(String mtaId) {
        attributeRestrictions.put(OperationDto.AttributeNames.MTA_ID, AttributeLiteralRestriction.of(criteriaBuilder::equal, mtaId));
        addEqualsRestriction(OperationDto.AttributeNames.MTA_ID, mtaId);
        return this;
    }

    @Override
    public OperationQuery acquiredLock(Boolean acquiredLock) {
        attributeRestrictions.put(OperationDto.AttributeNames.ACQUIRED_LOCK,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, acquiredLock));
        addEqualsRestriction(OperationDto.AttributeNames.ACQUIRED_LOCK, acquiredLock);
        return this;
    }

    @Override
    public OperationQuery state(State finalState) {
        attributeRestrictions.put(OperationDto.AttributeNames.FINAL_STATE,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, finalState));
        addEqualsRestriction(OperationDto.AttributeNames.FINAL_STATE, finalState);
        return this;
    }

    @Override
    public OperationQuery startedBefore(Date startedBefore) {
        attributeRestrictions.put(OperationDto.AttributeNames.STARTED_AT,
            AttributeRestriction.of(criteriaBuilder::lessThan, startedBefore));
        return this;
    }

    @Override
    public OperationQuery endedAfter(Date endedAfter) {
        attributeRestrictions.put(OperationDto.AttributeNames.ENDED_AT, AttributeRestriction.of(criteriaBuilder::greaterThan, endedAfter));
        return this;
    }

    @Override
    public OperationQuery inNonFinalState() {
        attributeRestrictions.put(OperationDto.AttributeNames.FINAL_STATE, AttributeLiteralRestriction.of(criteriaBuilder::equal, null));
        return this;
    }

    @Override
    public OperationQuery inFinalState() {
        attributeRestrictions.put(OperationDto.AttributeNames.FINAL_STATE, AttributeLiteralRestriction.of(criteriaBuilder::notEqual, null));
        return this;
    }

    @Override
    public OperationQuery withStateAnyOf(List<State> states) {
        attributeRestrictions.put(OperationDto.AttributeNames.FINAL_STATE, AttributeInCollectionRestriction.of(states.stream()
            .map(State::toString)
            .collect(Collectors.toList())));
        return this;
    }

    @Override
    public OperationQuery orderByProcessId(OrderDirection orderDirection) {
        setOrder(OperationDto.AttributeNames.PROCESS_ID, orderDirection);
        return this;
    }

    @Override
    public OperationQuery orderByEndTime(OrderDirection orderDirection) {
        setOrder(OperationDto.AttributeNames.ENDED_AT, orderDirection);
        return this;
    }

    @Override
    public OperationQuery orderByStartTime(OrderDirection orderDirection) {
        setOrder(OperationDto.AttributeNames.STARTED_AT, orderDirection);
        return this;
    }

    @Override
    public Operation singleResult() {
        OperationDto dto = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, OperationDto.class).getSingleResult());
        return operationFactory.fromDto(dto);
    }

    @Override
    public List<Operation> list() {
        List<OperationDto> dtos = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, OperationDto.class).getResultList());
        return dtos.stream()
            .map(operationFactory::fromDto)
            .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(entityManager,
            manager -> createDeleteQuery(manager, criteriaBuilder, attributeRestrictions, OperationDto.class).executeUpdate());
    }

}
