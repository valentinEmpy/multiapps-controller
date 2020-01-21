package com.sap.cloud.lm.sl.cf.core.cf.detect.entity;

import org.cloudfoundry.client.lib.domain.CloudService;

import com.sap.cloud.lm.sl.cf.core.model.ServiceMtaMetadata;

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
}
