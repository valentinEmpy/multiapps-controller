package com.sap.cloud.lm.sl.cf.core.cf.detect;

import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MtaMetadataEntityAggregator {

    @Autowired
    private MtaMetadataExtractorFactory<MtaMetadataEntity> metadataExtractorFactory;

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
        DeployedMta deployedMta = new DeployedMta();
        mtaMetadataEntities.forEach(mtaMetadataEntity -> metadataExtractorFactory.get(mtaMetadataEntity)
                                                                                 .extract(mtaMetadataEntity, deployedMta));
        return deployedMta;
    }

}
