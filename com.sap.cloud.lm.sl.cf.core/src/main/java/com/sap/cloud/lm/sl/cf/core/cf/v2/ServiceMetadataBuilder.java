package com.sap.cloud.lm.sl.cf.core.cf.v2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudfoundry.client.v3.Metadata;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.model.Resource;

public class ServiceMetadataBuilder {

    public static Metadata build(DeploymentDescriptor deploymentDescriptor, Resource resource, Map<String, Object> serviceParameters) {

        List<DeployedMtaModule> boundModules = deploymentDescriptor.getModules()
                                                                   .stream()
                                                                   .filter(module -> moduleContainsResource(module, resource))
                                                                   .map(ServiceMetadataBuilder::mapModuleToDeployedMtaModule)
                                                                   .collect(Collectors.toList());

        DeployedMtaResource deployedMtaResource = ImmutableDeployedMtaResource.builder()
                                                                              .serviceName(NameUtil.getServiceName(resource))
                                                                              .resourceName(resource.getName())
                                                                              .serviceInstanceParameters(serviceParameters)
                                                                              .modules(boundModules)
                                                                              .build();

        return Metadata.builder()
                       .label(MtaMetadataLabels.MTA_ID, deploymentDescriptor.getId())
                       .label(MtaMetadataLabels.MTA_VERSION, deploymentDescriptor.getVersion())
                       .annotation(MtaMetadataAnnotations.RESOURCE, JsonUtil.toJson(deployedMtaResource, true))
                       .build();
    }

    private static boolean moduleContainsResource(Module module, Resource resource) {
        return module.getRequiredDependencies()
                     .stream()
                     .anyMatch(dependency -> dependency.getName()
                                                       .equalsIgnoreCase(resource.getName()));
    }

    private static DeployedMtaModule mapModuleToDeployedMtaModule(Module module) {
        return ImmutableDeployedMtaModule.builder()
                                         .appName(NameUtil.getApplicationName(module))
                                         .moduleName(module.getName())
                                         .build();
    }
}
