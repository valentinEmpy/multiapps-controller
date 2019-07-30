package com.sap.cloud.lm.sl.cf.core.persistence.service;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationSubscriptionDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ConfigurationSubscriptionFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationSubscriptionQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.ConfigurationSubscriptionQueryImpl;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Component
public class ConfigurationSubscriptionService extends PersistenceService<ConfigurationSubscription, ConfigurationSubscriptionDto, Long> {

    @Inject
    private ConfigurationSubscriptionFactory subscriptionFactory;

    @Inject
    public ConfigurationSubscriptionService(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, ConfigurationSubscriptionDto.class);
    }

    public ConfigurationSubscriptionQuery createQuery() {
        return new ConfigurationSubscriptionQueryImpl(createEntityManager(), subscriptionFactory);
    }

    @Override
    protected ConfigurationSubscriptionDto merge(ConfigurationSubscriptionDto existingSubscription,
        ConfigurationSubscriptionDto newSubscription) {
        super.merge(existingSubscription, newSubscription);
        String mtaId = ObjectUtils.firstNonNull(newSubscription.getMtaId(), existingSubscription.getMtaId());
        String appName = ObjectUtils.firstNonNull(newSubscription.getAppName(), existingSubscription.getAppName());
        String spaceId = ObjectUtils.firstNonNull(newSubscription.getSpaceId(), existingSubscription.getSpaceId());
        String filter = ObjectUtils.firstNonNull(newSubscription.getFilter(), existingSubscription.getFilter());
        String moduleContent = ObjectUtils.firstNonNull(newSubscription.getModuleContent(), existingSubscription.getModuleContent());
        String resourceProperties = ObjectUtils.firstNonNull(newSubscription.getResourceProperties(),
            existingSubscription.getResourceProperties());
        String resourceName = ObjectUtils.firstNonNull(newSubscription.getResourceName(), existingSubscription.getResourceName());
        return new ConfigurationSubscriptionDto(newSubscription.getPrimaryKey(), mtaId, spaceId, appName, filter, moduleContent,
            resourceName, resourceProperties);
    }

    @Override
    protected PersistenceObjectFactory<ConfigurationSubscription, ConfigurationSubscriptionDto> getPersistenceObjectFactory() {
        return subscriptionFactory;
    }

    @Override
    protected void onEntityConflict(ConfigurationSubscriptionDto subscription, Throwable t) {
        throw (ConflictException) new ConflictException(Messages.CONFIGURATION_SUBSCRIPTION_ALREADY_EXISTS, subscription.getMtaId(),
            subscription.getAppName(), subscription.getResourceName(), subscription.getSpaceId()).initCause(t);
    }

    @Override
    protected void onEntityNotFound(Long id) {
        throw new NotFoundException(Messages.CONFIGURATION_SUBSCRIPTION_NOT_FOUND, id);
    }

}
