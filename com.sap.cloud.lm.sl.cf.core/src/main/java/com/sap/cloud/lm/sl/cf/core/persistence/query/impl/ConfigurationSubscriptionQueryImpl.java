package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationSubscriptionDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ConfigurationSubscriptionFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationSubscriptionQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeLiteralRestriction;

public class ConfigurationSubscriptionQueryImpl extends AbstractQueryImpl<ConfigurationSubscription, ConfigurationSubscriptionQuery>
    implements ConfigurationSubscriptionQuery {

    private EntityManager entityManager;
    private Map<String, AbstractAttributeRestriction> attributeRestrictions = new HashMap<>();
    private ConfigurationSubscriptionFactory subscriptionFactory;

    public ConfigurationSubscriptionQueryImpl(EntityManager entityManager, ConfigurationSubscriptionFactory subscriptionFactory) {
        this.entityManager = entityManager;
        this.subscriptionFactory = subscriptionFactory;
    }

    @Override
    public ConfigurationSubscriptionQuery id(Long id) {
        attributeRestrictions.put(ConfigurationSubscriptionDto.AttributeNames.ID,
            AttributeLiteralRestriction.of(getCriteriaBuilder()::equal, id));
        return this;
    }

    private CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Override
    public ConfigurationSubscriptionQuery mtaId(String mtaId) {
        attributeRestrictions.put(ConfigurationSubscriptionDto.AttributeNames.MTA_ID,
            AttributeLiteralRestriction.of(getCriteriaBuilder()::equal, mtaId));
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery spaceId(String spaceId) {
        attributeRestrictions.put(ConfigurationSubscriptionDto.AttributeNames.SPACE_ID,
            AttributeLiteralRestriction.of(getCriteriaBuilder()::equal, spaceId));
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery appName(String appName) {
        attributeRestrictions.put(ConfigurationSubscriptionDto.AttributeNames.APP_NAME,
            AttributeLiteralRestriction.of(getCriteriaBuilder()::equal, appName));
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery resourceName(String resourceName) {
        attributeRestrictions.put(ConfigurationSubscriptionDto.AttributeNames.RESOURCE_NAME,
            AttributeLiteralRestriction.of(getCriteriaBuilder()::equal, resourceName));
        return this;
    }

    @Override
    public ConfigurationSubscription singleResult() {
        ConfigurationSubscriptionDto dto = executeInTransaction(entityManager,
            manager -> createQuery(manager, getCriteriaBuilder(), attributeRestrictions, ConfigurationSubscriptionDto.class)
                .getSingleResult());
        return subscriptionFactory.fromDto(dto);
    }

    @Override
    public List<ConfigurationSubscription> list() {
        List<ConfigurationSubscriptionDto> dtos = executeInTransaction(entityManager,
            manager -> createQuery(manager, getCriteriaBuilder(), attributeRestrictions, ConfigurationSubscriptionDto.class)
                .getResultList());
        return dtos.stream()
            .map(subscriptionFactory::fromDto)
            .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(entityManager,
            manager -> createDeleteQuery(manager, getCriteriaBuilder(), attributeRestrictions, ConfigurationSubscriptionDto.class)
                .executeUpdate());
    }

}
