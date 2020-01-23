package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ServiceMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.ServiceMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.ServiceMtaMetadataExtractor;

@Component
public class ServiceMtaMetadataEntityCollector implements MtaMetadataEntityCollector<ServiceMtaMetadataEntity> {

    @Inject
    private ServiceMtaMetadataExtractor serviceMtaMetadataExtractor;

    @Override
    public List<ServiceMtaMetadataEntity> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        return client.getServicesByMetadataLabelSelector(criteria.get())
                     .stream()
                     .map(this::toMtaMetadataEntity)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    private ServiceMtaMetadataEntity toMtaMetadataEntity(CloudService service) {
        ServiceMtaMetadata serviceMtaMetadata = serviceMtaMetadataExtractor.extractMetadata(service);
        return serviceMtaMetadata != null ? new ServiceMtaMetadataEntity(service, serviceMtaMetadata) : null;
    }

}
