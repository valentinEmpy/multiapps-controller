package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.criteria.MtaMetadataCriteriaBuilder;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class DeployedComponentsDetector {

    @Autowired
    private List<MtaMetadataCollector<? extends MetadataEntity>> metadataCollectors;

    @Autowired
    private MtaMetadataEntityAggregator mtaMetadataEntityAggregator;

    public List<DeployedMta> getAllDeployedMtas(CloudControllerClient client) {
        MtaMetadataCriteria selectionCriteria = MtaMetadataCriteriaBuilder.builder()
                                                                          .label(MtaMetadataCriteriaBuilder.LABEL_MTA_ID)
                                                                          .exists()
                                                                          .build();
        List<DeployedMta> deployedMtas = getDeployedMtasByMetadataSelectionCriteria(selectionCriteria, client);
        for (DeployedMta deployedMtaByEnv : getDeployedMtasByEnv(client)) {
            if (!deployedMtas.contains(deployedMtaByEnv)) {
                deployedMtas.add(deployedMtaByEnv);
            }
        }
        return deployedMtas;
    }

    private List<DeployedMta> getDeployedMtasByEnv(CloudControllerClient client) {
        DeployedComponentsDetectorEnv envDeployedComponentsDetector = new DeployedComponentsDetectorEnv(client);
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

    private List<DeployedMta> getDeployedMtasByMetadataSelectionCriteria(MtaMetadataCriteria criteria, CloudControllerClient client) {
        Map<String, Map<Version, List<MetadataEntity>>> mtaEntitiesByIdByVersion = collectMtaEntitiesByMetadataSelectionCriteria(criteria,
                                                                                                                                 client);
        List<DeployedMta> deployedMtas = mtaEntitiesByIdByVersion.values()
                                                                 .stream()
                                                                 .flatMap(versionsMaps -> versionsMaps.values()
                                                                                                      .stream())
                                                                 .map(listEntitiesSameIdDifferentVersion -> mtaMetadataEntityAggregator.aggregate(listEntitiesSameIdDifferentVersion))
                                                                 .collect(Collectors.toList());
        return processDeployedMtas(deployedMtas);
    }

    private Map<String, Map<Version, List<MetadataEntity>>> collectMtaEntitiesByMetadataSelectionCriteria(MtaMetadataCriteria criteria,
                                                                                                          CloudControllerClient client) {
        return metadataCollectors.stream()
                                 .map(collector -> collector.collect(criteria, client))
                                 .flatMap(List::stream)
                                 .collect(Collectors.groupingBy(e -> e.getMtaMetadata()
                                                                      .getId(),
                                                                Collectors.groupingBy(e -> e.getMtaMetadata()
                                                                                            .getVersion())));
    }

    private List<DeployedMta> processDeployedMtas(List<DeployedMta> deployedMtas) {
        List<DeployedMta> mergedMtasById = mergeDifferentVersionsOfMtasWithSameId(deployedMtas);
        return removeEmptyMtas(mergedMtasById);
    }

    private List<DeployedMta> mergeDifferentVersionsOfMtasWithSameId(List<DeployedMta> mtas) {
        Map<String, Optional<DeployedMta>> deployedMtasById = mtas.stream()
                                                                  .collect(Collectors.groupingBy(e -> e.getMetadata()
                                                                                                       .getId(),
                                                                                                 Collectors.reducing(this::mergeMtas)));

        Collection<Optional<DeployedMta>> deployedMtas = deployedMtasById.values();

        return deployedMtas.stream()
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .collect(Collectors.toList());
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

    private Optional<DeployedMta> getDeployedMtaByEnv(String mtaId, CloudControllerClient client) {
        DeployedComponentsDetectorEnv envDeployedComponentsDetector = new DeployedComponentsDetectorEnv(client);
        List<DeployedMta> deployedMtas = envDeployedComponentsDetector.detectAllDeployedComponents();
        return deployedMtas.stream()
                           .filter(mta -> mta.getMetadata()
                                             .getId()
                                             .equalsIgnoreCase(mtaId))
                           .findFirst();
    }
}
