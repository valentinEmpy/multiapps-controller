package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.ApplicationMtaMetadataEnvExtractor;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.mta.model.Version;
import org.cloudfoundry.client.lib.CloudControllerClient;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ApplicationMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;

//TODO delete this class and its usages after the CF metadata becomes the go to metadata approach
//A release note should be already present explaining that the migration (at least one mta redeploy) is mandatory
public class DeployedMtaDetectorEnv {

    /**
     * Detects all deployed components on this platform.
     *
     */

    private final CloudControllerClient client;

    public DeployedMtaDetectorEnv(CloudControllerClient client) {
        this.client = client;
    }

    public List<DeployedMta> detectAllDeployedComponents() {
        List<DeployedMta> deployedMtas = client.getApplications()
                                               .stream()
                                               .map(ApplicationMtaMetadataEnvExtractor::parseAppMetadata)
                                               .map(this::getDeployedMta)
                                               .collect(Collectors.toList());
        return mergeDifferentVersionsOfMtasWithSameId(deployedMtas);
    }

    private DeployedMta getDeployedMta(ApplicationMtaMetadata appMetadata) {
        List<DeployedMtaResource> deployedMtaResources = appMetadata.getDeployedMtaModule()
                                                                    .getResources();
        List<DeployedMtaModule> deployedMtaModules = Collections.singletonList(appMetadata.getDeployedMtaModule());
        return DeployedMta.builder()
                          .withMetadata(appMetadata.getMtaMetadata())
                          .withModules(deployedMtaModules)
                          .withResources(deployedMtaResources)
                          .build();
    }

    private List<DeployedMta> mergeDifferentVersionsOfMtasWithSameId(List<DeployedMta> mtas) {
        Set<String> mtaIds = getMtaIds(mtas);
        List<DeployedMta> result = new ArrayList<>();
        for (String mtaId : mtaIds) {
            List<DeployedMta> mtasWithSameId = getMtasWithSameId(mtas, mtaId);
            if (mtasWithSameId.size() > 1) {
                result.add(mergeMtas(mtaId, mtasWithSameId));
            } else {
                result.add(mtasWithSameId.get(0));
            }
        }
        return result;
    }

    private Set<String> getMtaIds(List<DeployedMta> mtas) {
        return mtas.stream()
                   .map(mta -> mta.getMetadata()
                                  .getId())
                   .collect(Collectors.toSet());
    }

    private List<DeployedMta> getMtasWithSameId(List<DeployedMta> mtas, String id) {
        return mtas.stream()
                   .filter(mta -> mta.getMetadata()
                                     .getId()
                                     .equals(id))
                   .collect(Collectors.toList());
    }

    private DeployedMta mergeMtas(String mtaId, List<DeployedMta> mtas) {
        List<DeployedMtaModule> modules = new ArrayList<>();
        List<DeployedMtaResource> services = new ArrayList<>();
        Version currentVersion = null;
        for (DeployedMta mta : mtas) {
            currentVersion = compareAndGetVersion(currentVersion, mta.getMetadata()
                                                                     .getVersion());
            services.addAll(mta.getResources());
            modules.addAll(mta.getModules());
        }
        return DeployedMta.builder()
                          .withMetadata(new MtaMetadata(mtaId, currentVersion))
                          .withModules(modules)
                          .withResources(services)
                          .build();
    }

    private Version compareAndGetVersion(Version currentVersion, Version version) {
        if (currentVersion == null) {
            return version;
        }
        if (currentVersion.compareTo(version) == 0) {
            return version;
        }
        // Unknown version
        return new MtaMetadata().getVersion();
    }
}
