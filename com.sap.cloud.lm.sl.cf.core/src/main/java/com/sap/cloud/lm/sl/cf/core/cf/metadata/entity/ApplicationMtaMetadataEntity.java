package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity;

import org.cloudfoundry.client.lib.domain.CloudApplication;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ApplicationMtaMetadata;

public class ApplicationMtaMetadataEntity extends MtaMetadataEntity {

    private ApplicationMtaMetadata applicationMtaMetadata;
    private CloudApplication application;

    public ApplicationMtaMetadataEntity(CloudApplication application, ApplicationMtaMetadata applicationMtaMetadata) {
        super(applicationMtaMetadata.getMtaMetadata());
        this.applicationMtaMetadata = applicationMtaMetadata;
        this.application = application;
    }

    public ApplicationMtaMetadata getApplicationMtaMetadata() {
        return applicationMtaMetadata;
    }

    public CloudApplication getApplication() {
        return application;
    }

    @Override
    public void accept(MtaMetadataEntityVisitor mtaMetadataEntityVisitor) {
        mtaMetadataEntityVisitor.visit(this);
    }
}
