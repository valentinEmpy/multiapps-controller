package com.sap.cloud.lm.sl.cf.process.steps;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription.ResourceDto;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ConfigurationSubscriptionService;
import com.sap.cloud.lm.sl.cf.process.message.Messages;

@Named("createSubscriptionsStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CreateSubscriptionsStep extends SyncFlowableStep {

    @Inject
    private ConfigurationSubscriptionService configurationSubscriptionService;

    @Override
    protected StepPhase executeStep(ExecutionWrapper execution) {
        getStepLogger().debug(Messages.CREATING_SUBSCRIPTIONS);

        List<ConfigurationSubscription> subscriptions = StepsUtil.getSubscriptionsToCreate(execution.getContext());

        for (ConfigurationSubscription subscription : subscriptions) {
            createSubscription(subscription);
            getStepLogger().debug(Messages.CREATED_SUBSCRIPTION, subscription.getId());
        }

        getStepLogger().debug(Messages.SUBSCRIPTIONS_CREATED);
        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(DelegateExecution context) {
        return Messages.ERROR_CREATING_SUBSCRIPTIONS;
    }

    private ConfigurationSubscription detectSubscription(String mtaId, String applicationName, String spaceId, String resourceName) {
        return configurationSubscriptionService.createQuery()
                                               .appName(applicationName)
                                               .spaceId(spaceId)
                                               .resourceName(resourceName)
                                               .mtaId(mtaId)
                                               .singleResultOrNull();
    }

    private ConfigurationSubscription detectSubscription(ConfigurationSubscription subscription) {
        ResourceDto resourceDto = subscription.getResourceDto();
        if (resourceDto == null) {
            return null;
        }
        return detectSubscription(subscription.getMtaId(), subscription.getAppName(), subscription.getSpaceId(), resourceDto.getName());
    }

    protected void createSubscription(ConfigurationSubscription subscription) {
        infoSubscriptionCreation(subscription);
        ConfigurationSubscription existingSubscription = detectSubscription(subscription);
        if (existingSubscription != null) {
            configurationSubscriptionService.update(existingSubscription.getId(), subscription);
            return;
        }
        configurationSubscriptionService.add(subscription);
    }

    private void infoSubscriptionCreation(ConfigurationSubscription subscription) {
        if (subscription.getModuleDto() != null && subscription.getResourceDto() != null) {
            getStepLogger().info(MessageFormat.format(Messages.CREATING_SUBSCRIPTION_FROM_0_MODULE_TO_1_RESOURCE,
                                                      subscription.getModuleDto()
                                                                  .getName(),
                                                      subscription.getResourceDto()
                                                                  .getName()));
        }
    }

}
