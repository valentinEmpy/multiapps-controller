package com.sap.cloud.lm.sl.cf.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeployedMta {

    private MtaMetadata metadata;
    private List<DeployedMtaModule> modules;
    private List<DeployedMtaResource> services;

    private DeployedMta(Builder builder) {
        this.metadata = builder.metadata;
        this.modules = builder.modules;
        this.services = builder.services;
    }

    public DeployedMta() {
    }

    public DeployedMta(MtaMetadata metadata, List<DeployedMtaModule> modules, List<DeployedMtaResource> services) {
        this.metadata = metadata;
        this.modules = modules;
        this.services = services;
    }

    public MtaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MtaMetadata metadata) {
        this.metadata = metadata;
    }

    public List<DeployedMtaModule> getModules() {
        return modules;
    }

    public void setModules(List<DeployedMtaModule> modules) {
        this.modules = modules;
    }

    public List<DeployedMtaResource> getServices() {
        return services;
    }

    public void setServices(List<DeployedMtaResource> services) {
        this.services = services;
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        DeployedMta other = (DeployedMta) object;
        return Objects.equals(metadata, other.metadata);
    }

    public DeployedMtaModule findDeployedModule(String moduleName) {
        return getModules().stream()
                           .filter(module -> module.getModuleName()
                                                   .equalsIgnoreCase(moduleName))
                           .findFirst()
                           .orElse(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private MtaMetadata metadata;
        private List<DeployedMtaModule> modules = new ArrayList<>();
        private List<DeployedMtaResource> services = new ArrayList<>();

        private Builder() {
        }

        public Builder withMetadata(MtaMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withModules(List<DeployedMtaModule> modules) {
            this.modules = modules;
            return this;
        }

        public Builder withServices(List<DeployedMtaResource> services) {
            this.services = services;
            return this;
        }

        public DeployedMta build() {
            return new DeployedMta(this);
        }
    }

}
