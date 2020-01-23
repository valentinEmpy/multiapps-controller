package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;

@Component
public class MtaMetadataServiceCollector implements MtaMetadataEntityCollector<CloudService> {

    @Override
    public List<CloudService> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        return client.getServicesByMetadataLabelSelector(criteria.get());
    }
}
