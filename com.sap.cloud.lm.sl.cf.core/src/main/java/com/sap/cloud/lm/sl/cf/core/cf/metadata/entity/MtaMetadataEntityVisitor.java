package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity;

public interface MtaMetadataEntityVisitor {

    void visit(ApplicationMtaMetadataEntity applicationMtaMetadataEntity);

    void visit(ServiceMtaMetadataEntity serviceMtaMetadataEntity);

}
