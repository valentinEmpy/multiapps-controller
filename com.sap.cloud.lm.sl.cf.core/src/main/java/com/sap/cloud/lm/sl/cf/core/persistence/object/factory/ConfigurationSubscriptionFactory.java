package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import static java.text.MessageFormat.format;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationFilter;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription.ModuleDto;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription.ResourceDto;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationSubscriptionDto;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Component
public class ConfigurationSubscriptionFactory implements PersistenceObjectFactory<ConfigurationSubscription, ConfigurationSubscriptionDto> {

    @Override
    public ConfigurationSubscription fromDto(ConfigurationSubscriptionDto dto) {
        try {
            ConfigurationFilter parsedFilter = JsonUtil.fromJson(dto.getFilter(), ConfigurationFilter.class);
            Map<String, Object> parsedResourceProperties = JsonUtil.convertJsonToMap(dto.getResourceProperties());
            ResourceDto resourceDto = new ResourceDto(dto.getResourceName(), parsedResourceProperties);
            ModuleDto moduleDto = JsonUtil.fromJson(dto.getModuleContent(), ModuleDto.class);
            return new ConfigurationSubscription(dto.getPrimaryKey(),
                                                 dto.getMtaId(),
                                                 dto.getSpaceId(),
                                                 dto.getAppName(),
                                                 parsedFilter,
                                                 moduleDto,
                                                 resourceDto);
        } catch (SLException e) {
            throw new IllegalStateException(format(Messages.UNABLE_TO_PARSE_SUBSCRIPTION, e.getMessage()), e);
        }
    }

    @Override
    public ConfigurationSubscriptionDto toDto(ConfigurationSubscription subscription) {
        Long id = subscription.getId();
        String filter = null;
        if (subscription.getFilter() != null) {
            filter = JsonUtil.toJson(subscription.getFilter(), false);
        }
        ResourceDto resourceDto = subscription.getResourceDto();
        ModuleDto moduleDto = subscription.getModuleDto();
        String resourceProperties = null;
        String resourceName = null;
        if (resourceDto != null) {
            resourceProperties = JsonUtil.toJson(resourceDto.getProperties(), false);
            resourceName = resourceDto.getName();
        }
        String module = null;
        if (moduleDto != null) {
            module = JsonUtil.toJson(moduleDto, false);
        }
        String appName = subscription.getAppName();
        String spaceId = subscription.getSpaceId();
        String mtaId = subscription.getMtaId();
        return new ConfigurationSubscriptionDto(id, mtaId, spaceId, appName, filter, module, resourceName, resourceProperties);
    }

}