package com.sap.cloud.lm.sl.cf.core.cf.metadata;

import org.immutables.value.Value;

import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaApplication;

@Value.Immutable
public interface ApplicationMtaMetadata {

    MtaMetadata getMtaMetadata();

    DeployedMtaApplication getDeployedMtaApplication();

}
