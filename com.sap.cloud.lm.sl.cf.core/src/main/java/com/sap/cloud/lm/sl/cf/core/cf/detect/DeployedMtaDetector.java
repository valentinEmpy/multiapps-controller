package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteriaBuilder;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor.MtaMetadataEntityAggregator;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor.MtaMetadataEntityCollector;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;

@Component
public class DeployedMtaDetector {

    @Autowired
    private List<MtaMetadataEntityCollector<?>> mtaMetadataEntityCollectors;

    @Autowired
    private MtaMetadataEntityAggregator mtaMetadataEntityAggregator;

    public List<DeployedMta> getAllDeployedMtas(CloudControllerClient client) {
        MtaMetadataCriteria selectionCriteria = MtaMetadataCriteriaBuilder.builder()
                                                                          .label(MtaMetadataCriteriaBuilder.LABEL_MTA_ID)
                                                                          .exists()
                                                                          .build();
        List<DeployedMta> deployedMtas = getDeployedMtasByMetadataSelectionCriteria(selectionCriteria, client);
        List<DeployedMta> deployedMtasByEnv = getDeployedMtasByEnv(client).stream()
                                                                          .filter(deployedMtaByEnv -> !deployedMtas.contains(deployedMtaByEnv))
                                                                          .collect(Collectors.toList());
        return ListUtils.union(deployedMtas, deployedMtasByEnv);
    }

    private List<DeployedMta> getDeployedMtasByMetadataSelectionCriteria(MtaMetadataCriteria criteria, CloudControllerClient client) {
        List<MtaMetadataEntity> mtaMetadataEntities = mtaMetadataEntityCollectors.stream()
                                                                                 .map(collector -> collector.collect(criteria, client))
                                                                                 .flatMap(List::stream)
                                                                                 .collect(Collectors.toList());
        List<DeployedMta> deployedMtas = mtaMetadataEntityAggregator.aggregate(mtaMetadataEntities);
        return processDeployedMtas(deployedMtas);
    }

    private List<DeployedMta> processDeployedMtas(List<DeployedMta> deployedMtas) {
        List<DeployedMta> mergedMtasById = mergeDifferentVersionsOfMtasWithSameId(deployedMtas);
        return removeEmptyMtas(mergedMtasById);
    }

    private List<DeployedMta> mergeDifferentVersionsOfMtasWithSameId(List<DeployedMta> mtas) {
        Map<String, DeployedMta> deployedMtasById = mtas.stream()
                                                        .collect(Collectors.toMap(mta -> mta.getMetadata()
                                                                                            .getId(),
                                                                                  mta -> mta, this::mergeMtas));
        return new ArrayList<>(deployedMtasById.values());
    }

    private DeployedMta mergeMtas(DeployedMta from, DeployedMta to) {
        to.getResources()
          .addAll(from.getResources());
        to.getModules()
          .addAll(from.getModules());
        if (!from.getMetadata()
                 .getVersion()
                 .equals(to.getMetadata()
                           .getVersion())) {
            to.getMetadata()
              .setVersion(null);
        }
        return to;
    }

    private List<DeployedMta> removeEmptyMtas(List<DeployedMta> mtas) {
        return mtas.stream()
                   .filter(mta -> CollectionUtils.isNotEmpty(mta.getModules()) || CollectionUtils.isNotEmpty(mta.getResources()))
                   .collect(Collectors.toList());
    }

    private List<DeployedMta> getDeployedMtasByEnv(CloudControllerClient client) {
        DeployedMtaDetectorEnv envDeployedComponentsDetector = new DeployedMtaDetectorEnv(client);
        return envDeployedComponentsDetector.detectAllDeployedComponents();
    }

    public Optional<DeployedMta> getDeployedMta(String mtaId, CloudControllerClient client) {
        MtaMetadataCriteria selectionCriteria = MtaMetadataCriteriaBuilder.builder()
                                                                          .label(MtaMetadataCriteriaBuilder.LABEL_MTA_ID)
                                                                          .haveValue(mtaId)
                                                                          .build();
        List<DeployedMta> deployedMtasByMetadata = getDeployedMtasByMetadataSelectionCriteria(selectionCriteria, client);
        if (!deployedMtasByMetadata.isEmpty()) {
            return Optional.of(deployedMtasByMetadata.get(0));
        }
        return getDeployedMtaByEnv(mtaId, client);
    }

    private Optional<DeployedMta> getDeployedMtaByEnv(String mtaId, CloudControllerClient client) {
        DeployedMtaDetectorEnv envDeployedComponentsDetector = new DeployedMtaDetectorEnv(client);
        List<DeployedMta> deployedMtas = envDeployedComponentsDetector.detectAllDeployedComponents();
        return deployedMtas.stream()
                           .filter(mta -> mta.getMetadata()
                                             .getId()
                                             .equalsIgnoreCase(mtaId))
                           .findFirst();
    }
}
