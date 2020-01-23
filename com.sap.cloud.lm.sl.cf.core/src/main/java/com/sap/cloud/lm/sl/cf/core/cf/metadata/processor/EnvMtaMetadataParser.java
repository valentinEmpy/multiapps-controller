package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sap.cloud.lm.sl.cf.core.Constants;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaResource;
import com.sap.cloud.lm.sl.common.ParsingException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class EnvMtaMetadataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvMtaMetadataParser.class);
    private static final List<String> ENV_METADATA_FIELDS = Arrays.asList(Constants.ENV_MTA_METADATA, Constants.ENV_MTA_MODULE_METADATA,
                                                                         Constants.ENV_MTA_SERVICES,
                                                                         Constants.ENV_MTA_MODULE_PUBLIC_PROVIDED_DEPENDENCIES);

    public boolean hasMtaMetadata(CloudApplication application) {
        return !Collections.disjoint(application.getEnv()
                                                .keySet(),
                                     ENV_METADATA_FIELDS);
    }

    public MtaMetadata parseMtaMetadata(CloudApplication application) {
        try {
            if (!isMtaMetadataComplete(application)) {
                throw new ParsingException(Messages.MTA_METADATA_FOR_APP_0_IS_INCOMPLETE, application.getName());
            }
            String envMtaMetadata = application.getEnv()
                                               .get(Constants.ENV_MTA_METADATA);
            Map<String, Object> mtaMetadata = JsonUtil.convertJsonToMap(envMtaMetadata);
            String exceptionMessage = MessageFormat.format(Messages.ENV_OF_APP_0_CONTAINS_INVALID_VALUE_FOR_1, application.getName(),
                                                           Constants.ENV_MTA_METADATA);
            String id = (String) getRequired(mtaMetadata, Constants.ATTR_ID, exceptionMessage);
            String version = (String) getRequired(mtaMetadata, Constants.ATTR_VERSION, exceptionMessage);
            return ImmutableMtaMetadata.builder()
                                       .id(id)
                                       .version(Version.parseVersion(version))
                                       .build();
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_ENV_METADATA_FOR_APP_0, application.getName());
        }
    }

    private boolean isMtaMetadataComplete(CloudApplication application) {
        return application.getEnv()
                          .keySet()
                          .containsAll(ENV_METADATA_FIELDS);
    }

    private <K, V> V getRequired(Map<K, V> map, K key, String exceptionMessage) {
        V value = map.get(key);
        if (value == null) {
            throw new ParsingException(exceptionMessage);
        }
        return value;
    }

    public DeployedMtaModule parseModule(CloudApplication application) {
        try {
            if (!isMtaMetadataComplete(application)) {
                throw new ParsingException(Messages.MTA_METADATA_FOR_APP_0_IS_INCOMPLETE, application.getName());
            }
            String moduleName = parseModuleName(application);
            List<String> providedDependencyNames = parseProvidedDependencyNames(application);
            List<DeployedMtaResource> services = parseResources(application);
            return ImmutableDeployedMtaModule.builder()
                                             .appName(application.getName())
                                             .moduleName(moduleName)
                                             .providedDependencyNames(providedDependencyNames)
                                             .resources(services)
                                             .build();
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_ENV_METADATA_FOR_APP_0, application.getName());
        }
    }

    private String parseModuleName(CloudApplication application) {
        String envValue = application.getEnv()
                                     .get(Constants.ENV_MTA_MODULE_METADATA);
        Map<String, Object> mtaModuleMetadata = JsonUtil.convertJsonToMap(envValue);
        return (String) getRequired(mtaModuleMetadata, Constants.ATTR_NAME,
                                    MessageFormat.format(Messages.ENV_OF_APP_0_CONTAINS_INVALID_VALUE_FOR_1, application.getName(),
                                                         Constants.ENV_MTA_MODULE_METADATA));
    }

    private List<String> parseProvidedDependencyNames(CloudApplication application) {
        String envValue = application.getEnv()
                                     .get(Constants.ENV_MTA_MODULE_PUBLIC_PROVIDED_DEPENDENCIES);
        try {
            return JsonUtil.convertJsonToList(envValue, new TypeReference<List<String>>() {
            });
        } catch (ParsingException e) {
            LOGGER.warn(MessageFormat.format(Messages.COULD_NOT_PARSE_PROVIDED_DEPENDENCY_NAMES_1_OF_APP_0, application.getName(),
                                             envValue),
                        e);
            return Collections.emptyList();
        }
    }

    private List<DeployedMtaResource> parseResources(CloudApplication application) {
        return parseServices(application).stream()
                                         .map(name -> ImmutableDeployedMtaResource.builder()
                                                                                  .serviceName(name)
                                                                                  .build())
                                         .collect(Collectors.toList());
    }

    private List<String> parseServices(CloudApplication application) {
        String envValue = application.getEnv()
                                     .get(Constants.ENV_MTA_SERVICES);
        return JsonUtil.convertJsonToList(envValue, new TypeReference<List<String>>() {
        });
    }

}
