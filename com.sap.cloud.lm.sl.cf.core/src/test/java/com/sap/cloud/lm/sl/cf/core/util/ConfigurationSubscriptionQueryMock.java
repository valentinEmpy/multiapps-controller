package com.sap.cloud.lm.sl.cf.core.util;

import java.util.Collections;
import java.util.List;

import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationSubscriptionQuery;

public class ConfigurationSubscriptionQueryMock implements ConfigurationSubscriptionQuery {

    @Override
    public ConfigurationSubscriptionQuery limitOnSelect(int limit) {
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery offsetOnSelect(int offset) {
        return this;
    }

    @Override
    public ConfigurationSubscription singleResult() {
        return null;
    }

    @Override
    public ConfigurationSubscription singleResultOrNull() {
        return null;
    }

    @Override
    public List<ConfigurationSubscription> list() {
        return Collections.emptyList();
    }

    @Override
    public int delete() {
        return 0;
    }

    @Override
    public ConfigurationSubscriptionQuery id(Long id) {
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery mtaId(String mtaId) {
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery spaceId(String spaceId) {
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery appName(String appName) {
        return this;
    }

    @Override
    public ConfigurationSubscriptionQuery resourceName(String resourceName) {
        return this;
    }

}
