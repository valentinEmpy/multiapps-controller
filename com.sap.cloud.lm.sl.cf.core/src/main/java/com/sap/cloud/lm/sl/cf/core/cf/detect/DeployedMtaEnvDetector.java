package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.Constants;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.EnvMtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

//TODO delete this class and its usages after the CF metadata becomes the go to metadata approach
//A release note should be already present explaining that the migration (at least one mta redeploy) is mandatory
@Component
public class DeployedMtaEnvDetector {

    @Inject
    private EnvMtaMetadataParser envMtaMetadataParser;

    public List<DeployedMta> getAllDeployedMtas(CloudControllerClient client) {
        Map<String, List<CloudApplication>> applicationsByMtaId = getApplicationsByMtaId(client);
        return applicationsByMtaId.values()
                                  .stream()
                                  .map(this::toDeployedMta)
                                  .collect(Collectors.toList());
    }

    private Map<String, List<CloudApplication>> getApplicationsByMtaId(CloudControllerClient client) {
        Map<String, List<CloudApplication>> applicationsByMtaId = new HashMap<>();
        List<CloudApplication> applications = getApplicationsWithEnvMetadata(client);
        for (CloudApplication application : applications) {
            String mtaId = envMtaMetadataParser.parseMtaMetadata(application)
                                               .getId();
            if (applicationsByMtaId.containsKey(mtaId)) {
                applicationsByMtaId.get(mtaId)
                                   .add(application);
            } else {
                applicationsByMtaId.put(mtaId, new ArrayList<>(Arrays.asList(application)));
            }
        }
        return applicationsByMtaId;
    }

    private List<CloudApplication> getApplicationsWithEnvMetadata(CloudControllerClient client) {
        return client.getApplications()
                     .stream()
                     .filter(this::hasEnvMetadata)
                     .collect(Collectors.toList());
    }

    private boolean hasEnvMetadata(CloudApplication application) {
        return application.getEnv()
                          .containsKey(Constants.ENV_MTA_METADATA);
    }

    private DeployedMta toDeployedMta(List<CloudApplication> applications) {
        MtaMetadata mtaMetadata = getMtaMetadata(applications);
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

    private MtaMetadata getMtaMetadata(List<CloudApplication> applications) {
        String mtaId = envMtaMetadataParser.parseMtaMetadata(applications.get(0))
                                           .getId();
        Version version = getVersion(applications);
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(version)
                                   .build();
    }

    private Version getVersion(List<CloudApplication> applications) {
        if (allHaveSameMtaVersion(applications)) {
            return envMtaMetadataParser.parseMtaMetadata(applications.get(0))
                                       .getVersion();
        }
        return MtaMetadata.UNKNOWN_MTA_VERSION;
    }

    private boolean allHaveSameMtaVersion(List<CloudApplication> applications) {
        return applications.stream()
                           .map(entity -> envMtaMetadataParser.parseMtaMetadata(entity)
                                                              .getVersion())
                           .distinct()
                           .count() == 1;
    }

    public Optional<DeployedMta> getDeployedMta(String mtaId, CloudControllerClient client) {
        return getAllDeployedMtas(client).stream()
                                         .filter(mta -> mta.getMetadata()
                                                           .getId()
                                                           .equalsIgnoreCase(mtaId))
                                         .findFirst();
    }
}
