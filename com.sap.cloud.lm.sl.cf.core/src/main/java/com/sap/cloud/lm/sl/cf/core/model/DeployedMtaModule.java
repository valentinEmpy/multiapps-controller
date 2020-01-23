package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Date;
import java.util.List;

import org.cloudfoundry.client.lib.domain.annotation.Nullable;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ImmutableDeployedMtaModule.Builder.class)
public interface DeployedMtaModule {

    String getModuleName();

    String getAppName();

    @Nullable
    @Value.Auxiliary
    Date getCreatedOn();

    @Nullable
    @Value.Auxiliary
    Date getUpdatedOn();

    @Value.Auxiliary
    List<DeployedMtaResource> getResources();

    @Value.Auxiliary
    List<String> getProvidedDependencyNames();

    @Value.Auxiliary
    List<String> getUris();

}
