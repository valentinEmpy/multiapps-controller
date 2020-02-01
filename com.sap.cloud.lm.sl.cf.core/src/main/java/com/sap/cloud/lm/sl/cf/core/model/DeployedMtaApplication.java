package com.sap.cloud.lm.sl.cf.core.model;

import java.util.Date;
import java.util.List;

import org.cloudfoundry.client.lib.domain.annotation.Nullable;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ImmutableDeployedMtaApplication.Builder.class)
public interface DeployedMtaApplication {

    String getModuleName();

    String getAppName();

    @Nullable
    @Value.Auxiliary
    Date getCreatedOn();

    @Nullable
    @Value.Auxiliary
    Date getUpdatedOn();

    @Value.Auxiliary
    List<DeployedMtaService> getServices();

    @Value.Auxiliary
    List<String> getProvidedDependencyNames();

    @Value.Auxiliary
    List<String> getUris();

    @Value.Auxiliary
    @Value.Default
    @JsonIgnore
    default ProductizationState getProductizationState() {
        return ProductizationState.LIVE;
    }

    enum ProductizationState {
        LIVE, IDLE
    }

}
