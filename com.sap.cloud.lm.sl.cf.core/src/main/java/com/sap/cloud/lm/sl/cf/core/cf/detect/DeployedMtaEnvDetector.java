package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.EnvMtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

/**
 * Remains solely for backwards compatibility with the 'environment' approach of detecting MTAs. Once past the deprecation period this will
 * be deleted, allowing the 'metadata' {@link com.sap.cloud.lm.sl.cf.core.cf.detect.DeployedMtaDetector} to become the standard approach for
 * detecting MTAs.
 */
@Deprecated
@Named
public class DeployedMtaEnvDetector {

    private EnvMtaMetadataParser envMtaMetadataParser;

    @Inject
    public DeployedMtaEnvDetector(EnvMtaMetadataParser envMtaMetadataParser) {
        this.envMtaMetadataParser = envMtaMetadataParser;
    }

    public List<DeployedMta> detectDeployedMtas(CloudControllerClient client) {
        Map<String, List<CloudApplication>> applicationsByMtaId = getApplicationsWithEnvMetadata(client).stream()
                                                                                                        .collect(Collectors.groupingBy(this::getMtaId));
        return applicationsByMtaId.entrySet()
                                  .stream()
                                  .map(entry -> toDeployedMta(entry.getKey(), entry.getValue()))
                                  .collect(Collectors.toList());
    }

    private List<CloudApplication> getApplicationsWithEnvMetadata(CloudControllerClient client) {
        return client.getApplications()
                     .stream()
                     .filter(MtaMetadataUtil::hasEnvMtaMetadata)
                     .collect(Collectors.toList());
    }

    private String getMtaId(CloudApplication application) {
        return envMtaMetadataParser.parseMtaMetadata(application)
                                   .getId();
    }

    private DeployedMta toDeployedMta(String mtaId, List<CloudApplication> applications) {
        Version mtaVersion = getMtaVersion(applications);
        MtaMetadata mtaMetadata = getMtaMetadata(mtaId, mtaVersion);
        List<DeployedMtaModule> modules = new ArrayList<>();
        List<DeployedMtaResource> resources = new ArrayList<>();
        for (CloudApplication application : applications) {
            DeployedMtaModule module = envMtaMetadataParser.parseModule(application);
            modules.add(module);
            resources.addAll(module.getResources());
        }
        return ImmutableDeployedMta.builder()
                                   .metadata(mtaMetadata)
                                   .modules(modules)
                                   .resources(resources)
                                   .build();
    }

    private Version getMtaVersion(List<CloudApplication> applications) {
        Version currentVersion = null;
        for (CloudApplication application : applications) {
            Version version = envMtaMetadataParser.parseMtaMetadata(application)
                                                  .getVersion();
            if (currentVersion != null && !currentVersion.equals(version)) {
                currentVersion = null;
                break;
            }
            currentVersion = version;
        }
        return currentVersion;
    }

    private MtaMetadata getMtaMetadata(String mtaId, Version mtaVersion) {
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(mtaVersion)
                                   .build();
    }

    public Optional<DeployedMta> detectDeployedMta(String mtaId, CloudControllerClient client) {
        return detectDeployedMtas(client).stream()
                                         .filter(mta -> mta.getMetadata()
                                                           .getId()
                                                           .equalsIgnoreCase(mtaId))
                                         .findFirst();
    }
}
