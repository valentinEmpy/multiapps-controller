package com.sap.cloud.lm.sl.cf.core.cf.v2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudfoundry.client.v3.Metadata;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaService;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaService;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ServiceMetadataBuilder {

    public static Metadata build(DeploymentDescriptor deploymentDescriptor, Resource resource, Map<String, Object> serviceParameters) {

        List<DeployedMtaApplication> boundApps = deploymentDescriptor.getModules()
                                                                     .stream()
                                                                     .filter(module -> moduleContainsResource(module, resource))
                                                                     .map(ServiceMetadataBuilder::mapModuleToDeployedMtaApplication)
                                                                     .collect(Collectors.toList());

        DeployedMtaService deployedMtaService = ImmutableDeployedMtaService.builder()
                                                                           .serviceName(NameUtil.getServiceName(resource))
                                                                           .resourceName(resource.getName())
                                                                           .serviceInstanceParameters(serviceParameters)
                                                                           .applications(boundApps)
                                                                           .build();

        return Metadata.builder()
                       .label(MtaMetadataLabels.MTA_ID, deploymentDescriptor.getId())
                       .label(MtaMetadataLabels.MTA_VERSION, deploymentDescriptor.getVersion())
                       .annotation(MtaMetadataAnnotations.RESOURCE, JsonUtil.toJson(deployedMtaService, true))
                       .build();
    }

    private static boolean moduleContainsResource(Module module, Resource resource) {
        return module.getRequiredDependencies()
                     .stream()
                     .anyMatch(dependency -> dependency.getName()
                                                       .equalsIgnoreCase(resource.getName()));
    }

    private static DeployedMtaApplication mapModuleToDeployedMtaApplication(Module module) {
        return ImmutableDeployedMtaApplication.builder()
                                              .appName(NameUtil.getApplicationName(module))
                                              .moduleName(module.getName())
                                              .build();
    }
}
