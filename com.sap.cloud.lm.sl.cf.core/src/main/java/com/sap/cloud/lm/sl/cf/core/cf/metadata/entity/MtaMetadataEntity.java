package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;

public abstract class MtaMetadataEntity {

    private MtaMetadata mtaMetadata;

    public MtaMetadataEntity(MtaMetadata mtaMetadata) {
        this.mtaMetadata = mtaMetadata;
    }

    public MtaMetadata getMtaMetadata() {
        return mtaMetadata;
    }

    public void setMtaMetadata(MtaMetadata mtaMetadata) {
        this.mtaMetadata = mtaMetadata;
    }

    public abstract void accept(MtaMetadataEntityVisitor mtaMetadataEntityVisitor);
}
