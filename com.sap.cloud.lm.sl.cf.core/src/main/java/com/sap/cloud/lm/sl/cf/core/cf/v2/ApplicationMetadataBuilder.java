package com.sap.cloud.lm.sl.cf.core.cf.v2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.cloudfoundry.client.v3.Metadata;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaService;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaService;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.model.ProvidedDependency;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ApplicationMetadataBuilder {

    public static Metadata build(DeploymentDescriptor deploymentDescriptor, Module module, List<ResourceAndResourceType> resources,
                                 List<String> uris) {
        List<DeployedMtaService> deployedServices = resources.stream()
                                                             .map(resource -> mapResourceToDeployedMtaService(resource, module))
                                                             .collect(Collectors.toList());
        List<String> providedDependenciesNames = getProvidedDependencies(module);
        DeployedMtaApplication deployedMtaApplication = ImmutableDeployedMtaApplication.builder()
                                                                                       .moduleName(module.getName())
                                                                                       .appName(getApplicationName(module))
                                                                                       .providedDependencyNames(providedDependenciesNames)
                                                                                       .services(deployedServices)
                                                                                       .uris(uris)
                                                                                       .build();
        return Metadata.builder()
                       .label(MtaMetadataLabels.MTA_ID, deploymentDescriptor.getId())
                       .label(MtaMetadataLabels.MTA_VERSION, deploymentDescriptor.getVersion())
                       .annotation(MtaMetadataAnnotations.MODULE, JsonUtil.toJson(deployedMtaApplication, true))
                       .build();
    }

    private static String getApplicationName(Module module) {
        String appName = NameUtil.getApplicationName(module);
        return appName == null ? module.getName() : appName;
    }

    private static DeployedMtaService mapResourceToDeployedMtaService(ResourceAndResourceType appResource, Module module) {
        ResourceType resourceType = appResource.getResourceType();
        Resource resource = appResource.getResource();
        if (resourceType != ResourceType.USER_PROVIDED_SERVICE) {
            return ImmutableDeployedMtaService.builder()
                                              .serviceName(NameUtil.getServiceName(resource))
                                              .resourceName(resource.getName())
                                              .build();
        }

        List<DeployedMtaApplication> deployedMtaApps = Collections.singletonList(ImmutableDeployedMtaApplication.builder()
                                                                                                                .moduleName(module.getName())
                                                                                                                .appName(getApplicationName(module))
                                                                                                                .build());
        Map<String, Object> parameters = (Map<String, Object>) resource.getParameters()
                                                                       .getOrDefault(SupportedParameters.SERVICE_CONFIG,
                                                                                     Collections.emptyMap());
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        TreeMap<String, Object> credentials = new TreeMap<>(parameters);
        return ImmutableDeployedMtaService.builder()
                                          .resourceName(resource.getName())
                                          .serviceName(NameUtil.getServiceName(resource))
                                          .applications(deployedMtaApps)
                                          .serviceInstanceParameters(credentials)
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
