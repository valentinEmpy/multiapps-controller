package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.ApplicationMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntityVisitor;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.ServiceMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;

public class DeployedMtaFiller implements MtaMetadataEntityVisitor {

    private DeployedMta deployedMta = newDeployedMta();

    public DeployedMta fillUsing(List<MtaMetadataEntity> mtaMetadataEntities) {
        for (MtaMetadataEntity mtaMetadataEntity : mtaMetadataEntities) {
            mtaMetadataEntity.accept(this);
        }
        return deployedMta;
    }

    @Override
    public void visit(ServiceMtaMetadataEntity serviceMtaMetadataEntity) {
        initDeployedMtaMetadata(serviceMtaMetadataEntity.getMtaMetadata());
        DeployedMtaResource deployedMtaResource = serviceMtaMetadataEntity.getServiceMtaMetadata()
                                                                          .getDeployedMtaResource();
        List<DeployedMtaResource> resources = deployedMta.getResources();
        replaceResource(resources, deployedMtaResource);
    }

    private void initDeployedMtaMetadata(MtaMetadata mtaMetadata) {
        if (deployedMta.getMetadata() == null && mtaMetadata != null) {
            deployedMta.setMetadata(mtaMetadata);
        }
    }

    private void replaceResource(List<DeployedMtaResource> resources, DeployedMtaResource deployedMtaResource) {
        resources.removeIf(resource -> resource.equals(deployedMtaResource));
        resources.add(deployedMtaResource);
    }

    @Override
    public void visit(ApplicationMtaMetadataEntity applicationMtaMetadataEntity) {
        initDeployedMtaMetadata(applicationMtaMetadataEntity.getMtaMetadata());
        CloudApplication application = applicationMtaMetadataEntity.getApplication();
        DeployedMtaModule deployedMtaModule = deployedMta.getModules()
                                                         .stream()
                                                         .filter(mtaModule -> mtaModule.getAppName()
                                                                                       .equals(application.getName()))
                                                         .findFirst()
                                                         .orElse(addNewModule(deployedMta));
        updateDeployedMtaModule(deployedMtaModule, applicationMtaMetadataEntity.getApplicationMtaMetadata()
                                                                               .getDeployedMtaModule(),
                                application);
    }

    private DeployedMtaModule addNewModule(DeployedMta metadata) {
        DeployedMtaModule module = DeployedMtaModule.builder()
                                                    .build();
        metadata.getModules()
                .add(module);
        return module;
    }

    private void updateDeployedMtaModule(DeployedMtaModule newDeployedMtaModule, DeployedMtaModule deployedMtaModule,
                                         CloudApplication application) {
        String moduleName = (deployedMtaModule.getModuleName() != null) ? deployedMtaModule.getModuleName() : application.getName();
        List<String> providedDependencies = (deployedMtaModule.getProvidedDependencyNames() != null)
            ? deployedMtaModule.getProvidedDependencyNames()
            : new ArrayList<>();
        newDeployedMtaModule.setModuleName(moduleName);
        newDeployedMtaModule.setAppName(application.getName());
        newDeployedMtaModule.setCreatedOn(application.getMetadata()
                                                     .getCreatedAt());
        newDeployedMtaModule.setUpdatedOn(application.getMetadata()
                                                     .getUpdatedAt());
        newDeployedMtaModule.setProvidedDependencyNames(providedDependencies);
        newDeployedMtaModule.setUris(application.getUris());
        List<DeployedMtaResource> appServices = (deployedMtaModule.getResources() != null) ? deployedMtaModule.getResources()
            : new ArrayList<>();
        appServices.forEach(resource -> newDeployedMtaModule.getResources()
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

    private boolean containsResource(List<DeployedMtaResource> resources, DeployedMtaResource resource) {
        return resources.stream()
                        .anyMatch(moduleResource -> areSameResource(moduleResource, resource));
    }

    private boolean areSameResource(DeployedMtaResource first, DeployedMtaResource second) {
        if (first.getResourceName() != null) {
            return first.getResourceName()
                        .equalsIgnoreCase(second.getResourceName());
        } else if (first.getServiceName() != null) {
            return first.getServiceName()
                        .equalsIgnoreCase(second.getServiceName());
        }
        return false;
    }

    private DeployedMta newDeployedMta() {
        return DeployedMta.builder()
                          .withModules(new ArrayList<>())
                          .withResources(new ArrayList<>())
                          .build();
    }
}
