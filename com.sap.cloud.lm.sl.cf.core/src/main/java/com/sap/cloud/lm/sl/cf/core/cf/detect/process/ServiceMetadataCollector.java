package com.sap.cloud.lm.sl.cf.core.cf.detect.process;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataCollector;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ServiceMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.mapping.ServiceMetadataFieldExtractor;
import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.model.ServiceMtaMetadata;

@Component
public class ServiceMetadataCollector implements MtaMetadataCollector<ServiceMetadataEntity> {

    @Inject
    private ServiceMetadataFieldExtractor fieldExtractor;

    @Override
    public List<ServiceMetadataEntity> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        List<ServiceMetadataEntity> resultEntities = new ArrayList<>();

        List<CloudService> allServices = client.getServicesByMetadataLabelSelector(criteria.get());
        for (CloudService service : allServices) {
            ServiceMtaMetadata serviceMetadata = fieldExtractor.extractMetadata(service);

            if (serviceMetadata == null) {
                continue;
            }
            resultEntities.add(new ServiceMetadataEntity(serviceMetadata, service, serviceMetadata.getMtaMetadata()));
        }
        return resultEntities;
    }

}
