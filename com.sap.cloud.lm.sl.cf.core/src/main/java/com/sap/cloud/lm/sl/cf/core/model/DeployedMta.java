package com.sap.cloud.lm.sl.cf.core.model;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeployedMta {

    private MtaMetadata metadata;
    private List<DeployedMtaModule> modules = new ArrayList<>();
    private List<DeployedMtaResource> resources = new ArrayList<>();

    private DeployedMta(Builder builder) {
        this.metadata = builder.metadata;
        this.modules = builder.modules;
        this.resources = builder.resources;
    }

    public DeployedMta() {
    }

    public DeployedMta(MtaMetadata metadata, List<DeployedMtaModule> modules, List<DeployedMtaResource> resources) {
        this.metadata = metadata;
        this.modules = modules;
        this.resources = resources;
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

    public List<DeployedMtaResource> getResources() {
        return resources;
    }

    public void setResources(List<DeployedMtaResource> resources) {
        this.resources = resources;
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
        private List<DeployedMtaResource> resources = new ArrayList<>();

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

        public Builder withResources(List<DeployedMtaResource> resources) {
            this.resources = resources;
            return this;
        }

        public DeployedMta build() {
            return new DeployedMta(this);
        }
    }

}
