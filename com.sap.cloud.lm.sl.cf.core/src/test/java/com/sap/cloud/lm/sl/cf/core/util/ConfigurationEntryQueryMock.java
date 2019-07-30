package com.sap.cloud.lm.sl.cf.core.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationEntryQuery;

public class ConfigurationEntryQueryMock implements ConfigurationEntryQuery {

    @Override
    public ConfigurationEntryQuery limitOnSelect(int limit) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery offsetOnSelect(int offset) {
        return this;
    }

    @Override
    public ConfigurationEntry singleResult() {
        return null;
    }

    @Override
    public ConfigurationEntry singleResultOrNull() {
        return null;
    }

    @Override
    public List<ConfigurationEntry> list() {
        return Collections.emptyList();
    }

    @Override
    public int delete() {
        return 0;
    }

    @Override
    public ConfigurationEntryQuery id(Long id) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery providerNid(String providerNid) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery providerId(String providerId) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery target(CloudTarget targetOrg) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery requiredProperties(Map<String, Object> requiredProperties) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery spaceId(String spaceId) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery version(String version) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery visibilityTargets(List<CloudTarget> visibilityTargets) {
        return this;
    }

    @Override
    public ConfigurationEntryQuery likeMtaId(String mtaId) {
        return this;
    }

}
