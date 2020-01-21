package com.sap.cloud.lm.sl.cf.core.cf.detect.process;

import java.util.List;

import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataExtractor;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ServiceMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;

public class ServiceMtaMetadataExtractor implements MtaMetadataExtractor<ServiceMtaMetadataEntity> {

    @Override
    public void extract(ServiceMtaMetadataEntity metadataEntity, DeployedMta deployedMta) {
        initMetadata(metadataEntity, deployedMta);
        DeployedMtaResource deployedMtaResource = metadataEntity.getServiceMtaMetadata()
                                                                .getDeployedMtaResource();
        replaceResource(deployedMtaResource, deployedMta.getResources());
    }

    private void replaceResource(DeployedMtaResource deployedMtaResource, List<DeployedMtaResource> resources) {
        resources.removeIf(resource -> resource.equals(deployedMtaResource));
        resources.add(deployedMtaResource);
    }
}
