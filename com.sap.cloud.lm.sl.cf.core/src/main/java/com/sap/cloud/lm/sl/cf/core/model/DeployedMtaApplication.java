package com.sap.cloud.lm.sl.cf.core.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonSerialize(as = ImmutableDeployedMtaApplication.class)
@JsonDeserialize(builder = ImmutableDeployedMtaApplication.Builder.class)
public interface DeployedMtaApplication extends CloudApplication {

    String getModuleName();

    List<String> getBoundMtaServices();

    List<String> getProvidedDependencyNames();

    @Value.Default
    default ProductizationState getProductizationState() {
        return ProductizationState.LIVE;
    }

    enum ProductizationState {
        LIVE, IDLE
    }

}
