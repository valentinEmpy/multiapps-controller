package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Named
public class EnvMtaMetadataParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvMtaMetadataParser.class);

    private EnvMtaMetadataValidator envMtaMetadataValidator;

    @Inject
    public EnvMtaMetadataParser(EnvMtaMetadataValidator envMtaMetadataValidator) {
        this.envMtaMetadataValidator = envMtaMetadataValidator;
    }

    public MtaMetadata parseMtaMetadata(CloudApplication application) {
        envMtaMetadataValidator.validate(application);
        Map<String, Object> mtaMetadata = JsonUtil.convertJsonToMap(application.getEnv()
                                                                               .get(Constants.ENV_MTA_METADATA));
        String mtaId = (String) mtaMetadata.get(Constants.ATTR_ID);
        String version = (String) mtaMetadata.get(Constants.ATTR_VERSION);
        Version mtaVersion = parseMtaVersion(version, application.getName());
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(mtaVersion)
                                   .build();
    }

    private Version parseMtaVersion(String mtaVersion, String appName) {
        try {
            return mtaVersion == null ? null : Version.parseVersion(mtaVersion);
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_ENV_METADATA_VERSION_FOR_APP_0, appName);
        }
    }

    public DeployedMtaModule parseModule(CloudApplication application) {
        envMtaMetadataValidator.validate(application);
        String moduleName = parseModuleName(application);
        List<String> providedDependencyNames = parseProvidedDependencyNames(application);
        List<DeployedMtaResource> resources = parseResources(application);
        return ImmutableDeployedMtaModule.builder()
                                         .appName(application.getName())
                                         .moduleName(moduleName)
                                         .providedDependencyNames(providedDependencyNames)
                                         .resources(resources)
                                         .build();
    }

    private String parseModuleName(CloudApplication application) {
        String envMtaModuleMetadata = application.getEnv()
                                                 .get(Constants.ENV_MTA_MODULE_METADATA);
        Map<String, Object> mtaModuleMetadata = JsonUtil.convertJsonToMap(envMtaModuleMetadata);
        return (String) mtaModuleMetadata.get(Constants.ATTR_NAME);
    }

    private List<String> parseProvidedDependencyNames(CloudApplication application) {
        String envMtaModuleProvidedDependencies = application.getEnv()
                                                             .get(Constants.ENV_MTA_MODULE_PUBLIC_PROVIDED_DEPENDENCIES);
        try {
            return JsonUtil.convertJsonToList(envMtaModuleProvidedDependencies, new TypeReference<List<String>>() {
            });
        } catch (ParsingException e) {
            LOGGER.warn(MessageFormat.format(Messages.COULD_NOT_PARSE_PROVIDED_DEPENDENCY_NAMES_1_OF_APP_0, application.getName(),
                                             envMtaModuleProvidedDependencies),
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
