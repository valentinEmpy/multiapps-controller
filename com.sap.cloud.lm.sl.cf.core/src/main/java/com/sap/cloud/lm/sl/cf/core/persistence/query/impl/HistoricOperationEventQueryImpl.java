package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent;
import com.sap.cloud.lm.sl.cf.core.model.HistoricOperationEvent.EventType;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.HistoricOperationEventDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.HistoricOperationEventFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.HistoricOperationEventQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeLiteralRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeRestriction;

public class HistoricOperationEventQueryImpl extends AbstractQueryImpl<HistoricOperationEvent, HistoricOperationEventQuery>
    implements HistoricOperationEventQuery {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private Map<String, AbstractAttributeRestriction> attributeRestrictions = new HashMap<>();
    private HistoricOperationEventFactory historicOperationEventFactory;

    public HistoricOperationEventQueryImpl(EntityManager entityManager, HistoricOperationEventFactory historicOperationEventFactory) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.historicOperationEventFactory = historicOperationEventFactory;
    }

    @Override
    public HistoricOperationEventQuery id(Long id) {
        attributeRestrictions.put(HistoricOperationEventDto.AttributeNames.ID, AttributeLiteralRestriction.of(criteriaBuilder::equal, id));
        return this;
    }

    @Override
    public HistoricOperationEventQuery processId(String processId) {
        attributeRestrictions.put(HistoricOperationEventDto.AttributeNames.PROCESS_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, processId));
        return this;
    }

    @Override
    public HistoricOperationEventQuery type(EventType type) {
        attributeRestrictions.put(HistoricOperationEventDto.AttributeNames.TYPE,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, type));
        return this;
    }

    @Override
    public HistoricOperationEventQuery olderThan(Date time) {
        attributeRestrictions.put(HistoricOperationEventDto.AttributeNames.TIMESTAMP,
            AttributeRestriction.of(criteriaBuilder::lessThan, time));
        return this;
    }

    @Override
    public HistoricOperationEvent singleResult() {
        HistoricOperationEventDto dto = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, HistoricOperationEventDto.class).getSingleResult());
        return historicOperationEventFactory.fromDto(dto);
    }

    @Override
    public List<HistoricOperationEvent> list() {
        List<HistoricOperationEventDto> dtos = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, HistoricOperationEventDto.class).getResultList());
        return dtos.stream()
            .map(historicOperationEventFactory::fromDto)
            .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(entityManager,
            manager -> createDeleteQuery(manager, criteriaBuilder, attributeRestrictions, HistoricOperationEventDto.class).executeUpdate());
    }

}
