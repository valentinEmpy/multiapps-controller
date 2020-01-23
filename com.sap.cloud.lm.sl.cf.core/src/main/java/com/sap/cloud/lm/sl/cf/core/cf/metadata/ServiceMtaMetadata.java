package com.sap.cloud.lm.sl.cf.core.cf.metadata;

import org.immutables.value.Value;

import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;

@Value.Immutable
public interface ServiceMtaMetadata {

    MtaMetadata getMtaMetadata();

    DeployedMtaResource getDeployedMtaResource();

}
