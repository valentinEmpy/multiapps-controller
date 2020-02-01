package com.sap.cloud.lm.sl.cf.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;

@Value.Immutable
@JsonDeserialize(builder = ImmutableDeployedMta.Builder.class)
public interface DeployedMta {

    MtaMetadata getMetadata();

    @Value.Auxiliary
    List<DeployedMtaApplication> getApplications();

    @Value.Auxiliary
    List<DeployedMtaService> getServices();

}
