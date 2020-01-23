package com.sap.cloud.lm.sl.cf.core.cf.v2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.cloudfoundry.client.v3.Metadata;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.ApplicationMtaMetadataExtractor;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.model.ProvidedDependency;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ApplicationMetadataBuilder {

    public static Metadata build(DeploymentDescriptor deploymentDescriptor, Module module, List<ResourceAndResourceType> moduleResources,
                                 List<String> uris) {
        List<DeployedMtaResource> deployedResources = moduleResources.stream()
                                                                     .map(resource -> mapResourceToDeployedMtaResource(resource, module))
                                                                     .collect(Collectors.toList());
        List<String> providedDependenciesNames = getProvidedDependencies(module);
        DeployedMtaModule deployedMtaModule = DeployedMtaModule.builder()
                                                               .withModuleName(module.getName())
                                                               .withAppName(NameUtil.getApplicationName(module))
                                                               .withProvidedDependencyNames(providedDependenciesNames)
                                                               .withResources(deployedResources)
                                                               .withUris(uris)
                                                               .build();
        return Metadata.builder()
                       .label(MtaMetadataLabels.MTA_ID, deploymentDescriptor.getId())
                       .label(MtaMetadataLabels.MTA_VERSION, deploymentDescriptor.getVersion())
                       .annotation(ApplicationMtaMetadataExtractor.MODULE, JsonUtil.toJson(deployedMtaModule, true))
                       .build();
    }

    private static DeployedMtaResource mapResourceToDeployedMtaResource(ResourceAndResourceType applicationService, Module module) {
        ResourceType resourceType = applicationService.getResourceType();
        Resource resource = applicationService.getResource();
        if (resourceType != ResourceType.USER_PROVIDED_SERVICE) {
            return DeployedMtaResource.builder()
                                      .withServiceName(NameUtil.getServiceName(resource))
                                      .withResourceName(resource.getName())
                                      .build();
        }

        List<DeployedMtaModule> deployedMtaModules = Collections.singletonList(DeployedMtaModule.builder()
                                                                                                .withModuleName(module.getName())
                                                                                                .withAppName(NameUtil.getApplicationName(module))
                                                                                                .build());
        Map<String, Object> parameters = (Map<String, Object>) resource.getParameters()
                                                                       .getOrDefault(SupportedParameters.SERVICE_CONFIG,
                                                                                     Collections.emptyMap());
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        TreeMap<String, Object> credentials = new TreeMap<>(parameters);
        return DeployedMtaResource.builder()
                                  .withServiceName(resource.getName())
                                  .withServiceName(NameUtil.getServiceName(resource))
                                  .withModules(deployedMtaModules)
                                  .withServiceInstanceParameters(credentials)
                                  .build();
    }

    private static List<String> getProvidedDependencies(Module module) {
        return module.getProvidedDependencies()
                     .stream()
                     .filter(ProvidedDependency::isPublic)
                     .map(ProvidedDependency::getName)
                     .collect(Collectors.toList());
    }
}
