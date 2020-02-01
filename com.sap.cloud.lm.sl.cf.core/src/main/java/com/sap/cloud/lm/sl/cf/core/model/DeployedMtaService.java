package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ImmutableDeployedMtaService.Builder.class)
public interface DeployedMtaService {

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
    List<DeployedMtaApplication> getApplications();

    @Value.Auxiliary
    Map<String, Object> getServiceInstanceParameters();

}
