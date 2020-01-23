package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.MtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

@Named
public class MtaMetadataEntityAggregator {

    private MtaMetadataParser mtaMetadataParser;

    @Inject
    public MtaMetadataEntityAggregator(MtaMetadataParser mtaMetadataParser) {
        this.mtaMetadataParser = mtaMetadataParser;
    }

    public List<DeployedMta> aggregate(List<CloudEntity> entities) {
        Map<String, List<CloudEntity>> entitiesByMtaId = entities.stream()
                                                                 .collect(Collectors.groupingBy(this::getMtaId));
        return entitiesByMtaId.entrySet()
                              .stream()
                              .map(entry -> toDeployedMta(entry.getKey(), entry.getValue()))
                              .collect(Collectors.toList());
    }

    private String getMtaId(CloudEntity entity) {
        return mtaMetadataParser.parseMtaMetadata(entity)
                                .getId();
    }

    private DeployedMta toDeployedMta(String mtaId, List<CloudEntity> entities) {
        Version mtaVersion = getMtaVersion(entities);
        MtaMetadata mtaMetadata = getMtaMetadata(mtaId, mtaVersion);
        return ImmutableDeployedMta.builder()
                                   .metadata(mtaMetadata)
                                   .modules(getModules(entities))
                                   .resources(getResources(entities))
                                   .build();
    }

    private Version getMtaVersion(List<CloudEntity> entities) {
        Version currentVersion = null;
        for (CloudEntity entity : entities) {
            Version version = mtaMetadataParser.parseMtaMetadata(entity)
                                               .getVersion();
            if (currentVersion != null && !currentVersion.equals(version)) {
                currentVersion = null;
                break;
            }
            currentVersion = version;
        }
        return currentVersion;
    }

    private MtaMetadata getMtaMetadata(String mtaId, Version version) {
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(version)
                                   .build();
    }

    private List<DeployedMtaModule> getModules(List<CloudEntity> entities) {
        return entities.stream()
                       .filter(CloudApplication.class::isInstance)
                       .map(CloudApplication.class::cast)
                       .map(mtaMetadataParser::parseModule)
                       .collect(Collectors.toList());
    }

    private List<DeployedMtaResource> getResources(List<CloudEntity> entities) {
        return entities.stream()
                       .filter(CloudService.class::isInstance)
                       .map(CloudService.class::cast)
                       .map(mtaMetadataParser::parseResource)
                       .collect(Collectors.toList());
    }

}
