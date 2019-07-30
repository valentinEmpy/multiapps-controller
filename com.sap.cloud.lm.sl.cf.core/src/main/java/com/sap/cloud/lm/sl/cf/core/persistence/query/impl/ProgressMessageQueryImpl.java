package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.sap.cloud.lm.sl.cf.core.persistence.dto.ProgressMessageDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ProgressMessageFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ProgressMessageQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeLiteralRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeRestriction;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage;
import com.sap.cloud.lm.sl.cf.persistence.model.ProgressMessage.ProgressMessageType;

public class ProgressMessageQueryImpl extends AbstractQueryImpl<ProgressMessage, ProgressMessageQuery> implements ProgressMessageQuery {

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private Map<String, AbstractAttributeRestriction> attributeRestrictions = new HashMap<>();
    private ProgressMessageFactory progressMessageFactory;

    public ProgressMessageQueryImpl(EntityManager entityManager, ProgressMessageFactory progressMessageFactory) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.progressMessageFactory = progressMessageFactory;
    }

    @Override
    public ProgressMessageQuery id(Long id) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.ID, AttributeLiteralRestriction.of(criteriaBuilder::equal, id));
        return this;
    }

    @Override
    public ProgressMessageQuery processId(String processId) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.PROCESS_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, processId));
        return this;
    }

    @Override
    public ProgressMessageQuery taskId(String taskId) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.TASK_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, taskId));
        return this;
    }

    @Override
    public ProgressMessageQuery type(ProgressMessageType type) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.TYPE, AttributeLiteralRestriction.of(criteriaBuilder::equal, type));
        return this;
    }

    @Override
    public ProgressMessageQuery typeNot(ProgressMessageType type) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.TYPE, AttributeLiteralRestriction.of(criteriaBuilder::notEqual, type));
        return this;
    }

    @Override
    public ProgressMessageQuery text(String text) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.TEXT, AttributeLiteralRestriction.of(criteriaBuilder::equal, text));
        return this;
    }

    @Override
    public ProgressMessageQuery olderThan(Date time) {
        attributeRestrictions.put(ProgressMessageDto.AttributeNames.TIMESTAMP, AttributeRestriction.of(criteriaBuilder::lessThan, time));
        return this;
    }

    @Override
    public ProgressMessage singleResult() {
        ProgressMessageDto dto = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, ProgressMessageDto.class).getSingleResult());
        return progressMessageFactory.fromDto(dto);
    }

    @Override
    public List<ProgressMessage> list() {
        List<ProgressMessageDto> dtos = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, ProgressMessageDto.class).getResultList());
        return dtos.stream()
            .map(progressMessageFactory::fromDto)
            .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(entityManager,
            manager -> createDeleteQuery(manager, criteriaBuilder, attributeRestrictions, ProgressMessageDto.class).executeUpdate());
    }

}
