package com.sap.cloud.lm.sl.cf.core.cf.detect.process;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataEntityCollector;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ServiceMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.mapping.ServiceMtaMetadataExtractor;
import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.model.ServiceMtaMetadata;

@Component
public class ServiceMtaMetadataEntityCollector implements MtaMetadataEntityCollector<ServiceMtaMetadataEntity> {

    @Inject
    private ServiceMtaMetadataExtractor fieldExtractor;

    @Override
    public List<ServiceMtaMetadataEntity> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        List<ServiceMtaMetadataEntity> resultEntities = new ArrayList<>();

        List<CloudService> allServices = client.getServicesByMetadataLabelSelector(criteria.get());
        for (CloudService service : allServices) {
            ServiceMtaMetadata serviceMetadata = fieldExtractor.extractMetadata(service);

            if (serviceMetadata == null) {
                continue;
            }
            resultEntities.add(new ServiceMtaMetadataEntity(service, serviceMetadata));
        }
        return resultEntities;
    }

}
