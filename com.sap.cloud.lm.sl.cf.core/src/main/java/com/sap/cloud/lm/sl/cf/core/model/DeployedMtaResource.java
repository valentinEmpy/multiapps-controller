package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ImmutableDeployedMtaResource.Builder.class)
public interface DeployedMtaResource {

    @Nullable
    String getResourceName();

    String getServiceName();

    @Nullable
    @Value.Auxiliary
    Date getCreatedOn();

    @Nullable
    @Value.Auxiliary
    Date getUpdatedOn();

    @Value.Auxiliary
    List<DeployedMtaModule> getModules();

    @Value.Auxiliary
    Map<String, Object> getServiceInstanceParameters();

}
