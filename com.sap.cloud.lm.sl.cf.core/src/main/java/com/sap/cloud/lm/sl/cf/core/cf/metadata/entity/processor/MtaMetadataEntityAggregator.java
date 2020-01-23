package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class MtaMetadataEntityAggregator {

    public List<DeployedMta> aggregate(List<MtaMetadataEntity> entities) {
        Map<String, Map<Version, List<MtaMetadataEntity>>> entitiesByIdByVersion = getMtaMetadataEntitiesByIdByVersion(entities);
        return entitiesByIdByVersion.values()
                                    .stream()
                                    .flatMap(entitiesByVersion -> entitiesByVersion.values()
                                                                                   .stream())
                                    .map(this::aggregateMtaMetadataEntitiesWithSameId)
                                    .collect(Collectors.toList());
    }

    private Map<String, Map<Version, List<MtaMetadataEntity>>> getMtaMetadataEntitiesByIdByVersion(List<MtaMetadataEntity> entities) {
        return entities.stream()
                       .collect(Collectors.groupingBy(e -> e.getMtaMetadata()
                                                            .getId(),
                                                      Collectors.groupingBy(e -> e.getMtaMetadata()
                                                                                  .getVersion())));
    }

    private DeployedMta aggregateMtaMetadataEntitiesWithSameId(List<MtaMetadataEntity> mtaMetadataEntities) {
        return new DeployedMtaFiller().fillUsing(mtaMetadataEntities);
    }

}
