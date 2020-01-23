package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.MtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.model.ImmutableDeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class MtaMetadataEntityAggregator {

    @Inject
    private MtaMetadataParser mtaMetadataParser;

    public List<DeployedMta> aggregate(List<CloudEntity> entities) {
        Map<String, List<CloudEntity>> entitiesByMtaId = getEntitiesByMtaId(entities);
        return entitiesByMtaId.values()
                              .stream()
                              .map(this::toDeployedMta)
                              .collect(Collectors.toList());
    }

    private Map<String, List<CloudEntity>> getEntitiesByMtaId(List<CloudEntity> entities) {
        Map<String, List<CloudEntity>> entitiesByMtaId = new HashMap<>();
        for (CloudEntity entity : entities) {
            String mtaId = mtaMetadataParser.parseMtaMetadata(entity)
                                            .getId();
            if (entitiesByMtaId.containsKey(mtaId)) {
                entitiesByMtaId.get(mtaId)
                               .add(entity);
            } else {
                entitiesByMtaId.put(mtaId, new ArrayList<>(Arrays.asList(entity)));
            }
        }
        return entitiesByMtaId;
    }

    private DeployedMta toDeployedMta(List<CloudEntity> entities) {
        MtaMetadata mtaMetadata = getMtaMetadata(entities);
        return ImmutableDeployedMta.builder()
                                   .metadata(mtaMetadata)
                                   .modules(getModules(entities))
                                   .resources(getResources(entities))
                                   .build();
    }

    private MtaMetadata getMtaMetadata(List<CloudEntity> entities) {
        String mtaId = mtaMetadataParser.parseMtaMetadata(entities.get(0))
                                        .getId();
        Version version = getVersion(entities);
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(version)
                                   .build();
    }

    private Version getVersion(List<CloudEntity> entities) {
        if (allHaveSameMtaVersion(entities)) {
            return mtaMetadataParser.parseMtaMetadata(entities.get(0))
                                    .getVersion();
        }
        return MtaMetadata.UNKNOWN_MTA_VERSION;
    }

    private boolean allHaveSameMtaVersion(List<CloudEntity> entities) {
        return entities.stream()
                       .map(entity -> mtaMetadataParser.parseMtaMetadata(entity)
                                                       .getVersion())
                       .distinct()
                       .count() == 1;
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
