package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections4.ListUtils;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteriaBuilder;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor.MtaMetadataEntityAggregator;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor.MtaMetadataEntityCollector;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;

@Component
public class DeployedMtaDetector {

    @Inject
    private List<MtaMetadataEntityCollector<?>> mtaMetadataEntityCollectors;

    @Inject
    private MtaMetadataEntityAggregator mtaMetadataEntityAggregator;

    @Inject
    private DeployedMtaEnvDetector deployedMtaEnvDetector;

    public List<DeployedMta> getAllDeployedMtas(CloudControllerClient client) {
        MtaMetadataCriteria selectionCriteria = MtaMetadataCriteriaBuilder.builder()
                                                                          .label(MtaMetadataCriteriaBuilder.LABEL_MTA_ID)
                                                                          .exists()
                                                                          .build();
        List<DeployedMta> deployedMtas = getDeployedMtasByMetadataSelectionCriteria(selectionCriteria, client);
        List<DeployedMta> deployedMtasByEnv = deployedMtaEnvDetector.getAllDeployedMtas(client)
                                                                    .stream()
                                                                    .filter(deployedMtaByEnv -> !deployedMtas.contains(deployedMtaByEnv))
                                                                    .collect(Collectors.toList());
        return ListUtils.union(deployedMtas, deployedMtasByEnv);
    }

    private List<DeployedMta> getDeployedMtasByMetadataSelectionCriteria(MtaMetadataCriteria criteria, CloudControllerClient client) {
        List<CloudEntity> mtaMetadataEntities = mtaMetadataEntityCollectors.stream()
                                                                           .map(collector -> collector.collect(criteria, client))
                                                                           .flatMap(List::stream)
                                                                           .collect(Collectors.toList());
        return mtaMetadataEntityAggregator.aggregate(mtaMetadataEntities);
    }

    public Optional<DeployedMta> getDeployedMta(String mtaId, CloudControllerClient client) {
        MtaMetadataCriteria selectionCriteria = MtaMetadataCriteriaBuilder.builder()
                                                                          .label(MtaMetadataCriteriaBuilder.LABEL_MTA_ID)
                                                                          .haveValue(mtaId)
                                                                          .build();
        List<DeployedMta> deployedMtasByMetadata = getDeployedMtasByMetadataSelectionCriteria(selectionCriteria, client);
        if (!deployedMtasByMetadata.isEmpty()) {
            return deployedMtasByMetadata.stream()
                                         .findFirst();
        }
        return deployedMtaEnvDetector.getDeployedMta(mtaId, client);
    }

}
