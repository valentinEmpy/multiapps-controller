package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.commons.lang3.StringUtils;

import com.sap.cloud.lm.sl.cf.core.filters.ContentFilter;
import com.sap.cloud.lm.sl.cf.core.filters.TargetWildcardFilter;
import com.sap.cloud.lm.sl.cf.core.filters.VersionFilter;
import com.sap.cloud.lm.sl.cf.core.filters.VisibilityFilter;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationEntryDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ConfigurationEntryFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationEntryQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeLiteralRestriction;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AttributeStringRestriction;

public class ConfigurationEntryQueryImpl extends AbstractQueryImpl<ConfigurationEntry, ConfigurationEntryQuery>
    implements ConfigurationEntryQuery {

    private static final BiPredicate<ConfigurationEntry, String> VERSION_FILTER = new VersionFilter();
    private static final BiPredicate<ConfigurationEntry, List<CloudTarget>> VISIBILITY_FILTER = new VisibilityFilter();
    private static final BiPredicate<CloudTarget, CloudTarget> TARGET_WILDCARD_FILTER = new TargetWildcardFilter();
    private static final BiPredicate<String, Map<String, Object>> CONTENT_FILTER = new ContentFilter();

    private EntityManager entityManager;
    private CriteriaBuilder criteriaBuilder;
    private Map<String, AbstractAttributeRestriction> attributeRestrictions = new HashMap<>();
    private ConfigurationEntryFactory entryFactory;
    private Map<String, Object> requiredProperties;
    private CloudTarget target;
    private List<CloudTarget> visibilityTargets;
    private String version;

    public ConfigurationEntryQueryImpl(EntityManager entityManager, ConfigurationEntryFactory entryFactory) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.entryFactory = entryFactory;
    }

    @Override
    public ConfigurationEntryQuery id(Long id) {
        attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.ID, AttributeLiteralRestriction.of(criteriaBuilder::equal, id));
        return this;
    }

    @Override
    public ConfigurationEntryQuery providerNid(String providerNid) {
        attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.PROVIDER_NID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, providerNid));
        return this;
    }

    @Override
    public ConfigurationEntryQuery providerId(String providerId) {
        attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.PROVIDER_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, providerId));
        return this;
    }

    @Override
    public ConfigurationEntryQuery target(CloudTarget target) {
        this.target = target;
        if (target != null && !StringUtils.isEmpty(target.getSpace())) {
            attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.TARGET_SPACE,
                AttributeLiteralRestriction.of(criteriaBuilder::equal, target.getSpace()));
        }
        if (target != null && !StringUtils.isEmpty(target.getOrg())) {
            attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.TARGET_ORG,
                AttributeLiteralRestriction.of(criteriaBuilder::equal, target.getOrg()));
        }
        return this;
    }

    @Override
    public ConfigurationEntryQuery version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public ConfigurationEntryQuery visibilityTargets(List<CloudTarget> visibilityTargets) {
        this.visibilityTargets = visibilityTargets;
        return this;
    }

    @Override
    public ConfigurationEntryQuery requiredProperties(Map<String, Object> requiredProperties) {
        this.requiredProperties = requiredProperties;
        return this;
    }

    @Override
    public ConfigurationEntryQuery spaceId(String spaceId) {
        attributeRestrictions.put(ConfigurationEntryDto.AttributeNames.SPACE_ID,
            AttributeLiteralRestriction.of(criteriaBuilder::equal, spaceId));
        return this;
    }

    @Override
    public ConfigurationEntryQuery likeMtaId(String mtaId) {
        attributeRestrictions.putIfAbsent(ConfigurationEntryDto.AttributeNames.PROVIDER_ID,
            AttributeStringRestriction.of(criteriaBuilder::like, mtaId + ":%"));
        return this;
    }

    @Override
    public ConfigurationEntry singleResult() {
        ConfigurationEntryDto dto = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, ConfigurationEntryDto.class).getSingleResult());
        ConfigurationEntry entry = entryFactory.fromDto(dto);
        if (satisfiesVersion(entry) && satisfiesVisibilityTargets(entry)) {
            return entryFactory.fromDto(dto);
        }
        throw new NoResultException("TODOMSG");
    }

    @Override
    public List<ConfigurationEntry> list() {
        List<ConfigurationEntryDto> dtos = executeInTransaction(entityManager,
            manager -> createQuery(manager, criteriaBuilder, attributeRestrictions, ConfigurationEntryDto.class).getResultList());
        if (version != null) {
        }
        return dtos.stream()
            .filter(this::satisfiesTargetWildcard)
            .filter(this::satisfiesContent)
            .map(entryFactory::fromDto)
            .filter(this::satisfiesVersion)
            .filter(this::satisfiesVisibilityTargets)
            .collect(Collectors.toList());
    }

    private boolean satisfiesVersion(ConfigurationEntry entry) {
        return VERSION_FILTER.test(entry, version);
    }

    private boolean satisfiesVisibilityTargets(ConfigurationEntry entry) {
        return VISIBILITY_FILTER.test(entry, visibilityTargets);
    }

    private boolean satisfiesTargetWildcard(ConfigurationEntryDto entryDto) {
        return TARGET_WILDCARD_FILTER.test(new CloudTarget(entryDto.getTargetOrg(), entryDto.getTargetSpace()), target);
    }

    private Boolean satisfiesContent(ConfigurationEntryDto entryDto) {
        return CONTENT_FILTER.test(entryDto.getContent(), requiredProperties);
    }

    @Override
    public int delete() {
        return executeInTransaction(entityManager,
            manager -> createDeleteQuery(manager, criteriaBuilder, attributeRestrictions, ConfigurationEntryDto.class).executeUpdate());
    }

}
