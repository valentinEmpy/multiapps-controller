package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntityVisitor;
import org.cloudfoundry.client.lib.domain.CloudService;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ServiceMtaMetadata;

public class ServiceMtaMetadataEntity extends MtaMetadataEntity {

    private ServiceMtaMetadata serviceMtaMetadata;
    private CloudService service;

    public ServiceMtaMetadataEntity(CloudService service, ServiceMtaMetadata serviceMtaMetadata) {
        super(serviceMtaMetadata.getMtaMetadata());
        this.serviceMtaMetadata = serviceMtaMetadata;
        this.service = service;
    }

    public ServiceMtaMetadata getServiceMtaMetadata() {
        return serviceMtaMetadata;
    }

    public CloudService getService() {
        return service;
    }

    @Override
    public void accept(MtaMetadataEntityVisitor mtaMetadataEntityVisitor) {
        mtaMetadataEntityVisitor.visit(this);
    }
}
