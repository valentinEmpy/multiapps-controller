package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ApplicationMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ServiceMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class MtaMetadataEntityAggregator {

    public List<DeployedMta> aggregate(List<MtaMetadataEntity> entities) {
        Map<String, Map<Version, List<MtaMetadataEntity>>> entitiesByIdByVersion = getMtaMetadataEntitiesByIdByVersion(entities);
        return entitiesByIdByVersion.values()
                                    .stream()
                                    .flatMap(entitiesByVersion -> entitiesByVersion.values()
                                                                                   .stream())
                                    .map(this::aggregateMtaMetadataEntitiesWithSameId)
                                    .collect(Collectors.toList());
    }

    private Map<String, Map<Version, List<MtaMetadataEntity>>> getMtaMetadataEntitiesByIdByVersion(List<MtaMetadataEntity> entities) {
        return entities.stream()
                       .collect(Collectors.groupingBy(e -> e.getMtaMetadata()
                                                            .getId(),
                                                      Collectors.groupingBy(e -> e.getMtaMetadata()
                                                                                  .getVersion())));
    }

    private DeployedMta aggregateMtaMetadataEntitiesWithSameId(List<MtaMetadataEntity> mtaMetadataEntities) {
        DeployedMta deployedMta = newDeployedMta();
        mtaMetadataEntities.forEach(mtaMetadataEntity -> extractMetadata(mtaMetadataEntity, deployedMta));
        return deployedMta;
    }

    private DeployedMta newDeployedMta() {
        return DeployedMta.builder()
                          .withModules(new ArrayList<>())
                          .withResources(new ArrayList<>())
                          .build();
    }

    private void extractMetadata(MtaMetadataEntity mtaMetadataEntity, DeployedMta deployedMta) {
        if (deployedMta.getMetadata() == null && mtaMetadataEntity.getMtaMetadata() != null) {
            deployedMta.setMetadata(mtaMetadataEntity.getMtaMetadata());
        }
        if (mtaMetadataEntity instanceof ApplicationMtaMetadataEntity) {
            extractApplicationMetadata((ApplicationMtaMetadataEntity) mtaMetadataEntity, deployedMta);
        } else if (mtaMetadataEntity instanceof ServiceMtaMetadataEntity) {
            extractServiceMetadata((ServiceMtaMetadataEntity) mtaMetadataEntity, deployedMta);
        }
    }

    private void extractApplicationMetadata(ApplicationMtaMetadataEntity applicationMtaMetadataEntity, DeployedMta deployedMta) {
        CloudApplication application = applicationMtaMetadataEntity.getApplication();
        DeployedMtaModule deployedMtaModule = applicationMtaMetadataEntity.getApplicationMtaMetadata()
                                                                          .getDeployedMtaModule();

        DeployedMtaModule module = getModule(deployedMta, application);

        String moduleName = (deployedMtaModule.getModuleName() != null) ? deployedMtaModule.getModuleName() : application.getName();

        List<String> providedDependencies = (deployedMtaModule.getProvidedDependencyNames() != null)
            ? deployedMtaModule.getProvidedDependencyNames()
            : new ArrayList<>();

        List<DeployedMtaResource> appServices = (deployedMtaModule.getResources() != null) ? deployedMtaModule.getResources()
            : new ArrayList<>();

        Date createdOn = application.getMetadata()
                                    .getCreatedAt();
        Date updatedOn = application.getMetadata()
                                    .getUpdatedAt();

        module.setModuleName(moduleName);
        module.setAppName(application.getName());
        module.setCreatedOn(createdOn);
        module.setUpdatedOn(updatedOn);
        module.setProvidedDependencyNames(providedDependencies);
        module.setUris(application.getUris());

        appServices.forEach(resource -> module.getResources()
                                              .add(resource));

        /*
         * Do not replace existing resources. They might be created by service metadata extraction. This is here only to move the user
         * provided service metadata to the service metadata because of v3 metadata api limitations regarding user provided services.
         */
        appServices.stream()
                   .filter(resource -> !containsResource(deployedMta.getResources(), resource))
                   .forEach(resource -> deployedMta.getResources()
                                                   .add(resource));
    }

    private DeployedMtaModule getModule(DeployedMta deployedMta, CloudApplication application) {
        return deployedMta.getModules()
                          .stream()
                          .filter(mtaModule -> mtaModule.getAppName()
                                                        .equalsIgnoreCase(application.getName()))
                          .findFirst()
                          .orElse(addNewModule(deployedMta));
    }

    private boolean containsResource(List<DeployedMtaResource> resources, DeployedMtaResource resource) {
        boolean containsByResourceName = resources.stream()
                                                  .filter(moduleResource -> moduleResource.getResourceName() != null)
                                                  .anyMatch(moduleResource -> moduleResource.getResourceName()
                                                                                            .equalsIgnoreCase(resource.getResourceName()));
        return containsByResourceName || resources.stream()
                                                  .filter(moduleResource -> moduleResource.getServiceName() != null)
                                                  .anyMatch(moduleResource -> moduleResource.getServiceName()
                                                                                            .equalsIgnoreCase(resource.getServiceName()));
    }

    private DeployedMtaModule addNewModule(DeployedMta metadata) {
        DeployedMtaModule module = DeployedMtaModule.builder()
                                                    .build();
        metadata.getModules()
                .add(module);
        return module;
    }

    private void extractServiceMetadata(ServiceMtaMetadataEntity serviceMtaMetadataEntity, DeployedMta deployedMta) {
        DeployedMtaResource deployedMtaResource = serviceMtaMetadataEntity.getServiceMtaMetadata()
                                                                          .getDeployedMtaResource();
        List<DeployedMtaResource> resources = deployedMta.getResources();
        resources.removeIf(resource -> resource.equals(deployedMtaResource));
        resources.add(deployedMtaResource);
    }

}
