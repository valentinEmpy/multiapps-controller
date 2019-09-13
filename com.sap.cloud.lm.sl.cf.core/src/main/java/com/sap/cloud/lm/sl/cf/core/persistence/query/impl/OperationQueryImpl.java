package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import com.sap.cloud.lm.sl.cf.core.persistence.OrderDirection;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.OperationDto;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.OperationDto.AttributeNames;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.OperationFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.OperationQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.criteria.ImmutableQueryAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.query.criteria.QueryCriteria;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.ProcessType;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

public class OperationQueryImpl extends AbstractQueryImpl<Operation, OperationQuery> implements OperationQuery {

    private QueryCriteria queryCriteria = new QueryCriteria();
    private OperationFactory operationFactory;

    public OperationQueryImpl(EntityManager entityManager, OperationFactory operationFactory) {
        super(entityManager);
        this.operationFactory = operationFactory;
    }

    @Override
    public OperationQuery processId(String processId) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.PROCESS_ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(processId)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery processType(ProcessType processType) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.PROCESS_TYPE)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(processType)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery user(String user) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.USER)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(user)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery spaceId(String spaceId) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.SPACE_ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(spaceId)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery mtaId(String mtaId) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.MTA_ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(mtaId)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery acquiredLock(Boolean acquiredLock) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.ACQUIRED_LOCK)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(acquiredLock)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery state(State finalState) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.FINAL_STATE)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(finalState)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery startedBefore(Date startedBefore) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.<Date> builder()
                                                                       .attribute(AttributeNames.STARTED_AT)
                                                                       .condition(getCriteriaBuilder()::lessThan)
                                                                       .value(startedBefore)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery endedAfter(Date endedAfter) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.<Date> builder()
                                                                       .attribute(AttributeNames.ENDED_AT)
                                                                       .condition(getCriteriaBuilder()::greaterThan)
                                                                       .value(endedAfter)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery inNonFinalState() {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.FINAL_STATE)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(null)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery inFinalState() {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.FINAL_STATE)
                                                                       .condition(getCriteriaBuilder()::notEqual)
                                                                       .value(null)
                                                                       .build());
        return this;
    }

    @Override
    public OperationQuery withStateAnyOf(List<State> states) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.<List<State>> builder()
                                                                       .attribute(AttributeNames.FINAL_STATE)
                                                                       .condition((state, expectedStates) -> isInCollection(state,
                                                                                                                            expectedStates))
                                                                       .value(states)
                                                                       .build());
        return this;
    }

    private <T> Predicate isInCollection(Expression attribute, List<T> values) {
        return attribute.in(values);
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
        OperationDto dto = executeInTransaction(manager -> createQuery(manager, queryCriteria, OperationDto.class).getSingleResult());
        return operationFactory.fromDto(dto);
    }

    @Override
    public List<Operation> list() {
        List<OperationDto> dtos = executeInTransaction(manager -> createQuery(manager, queryCriteria, OperationDto.class).getResultList());
        return dtos.stream()
                   .map(operationFactory::fromDto)
                   .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(manager -> createDeleteQuery(manager, queryCriteria, OperationDto.class).executeUpdate());
    }

}