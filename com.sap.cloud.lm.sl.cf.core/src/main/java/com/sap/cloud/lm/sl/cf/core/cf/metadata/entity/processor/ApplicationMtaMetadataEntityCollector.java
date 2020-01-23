package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ApplicationMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.ApplicationMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.ApplicationMtaMetadataExtractor;

@Component
public class ApplicationMtaMetadataEntityCollector implements MtaMetadataEntityCollector<ApplicationMtaMetadataEntity> {

    @Inject
    private ApplicationMtaMetadataExtractor applicationMtaMetadataExtractor;

    @Override
    public List<ApplicationMtaMetadataEntity> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        return client.getApplicationsByMetadataLabelSelector(criteria.get())
                     .stream()
                     .map(this::toMtaMetadataEntity)
                     .collect(Collectors.toList());
    }

    private ApplicationMtaMetadataEntity toMtaMetadataEntity(CloudApplication application) {
        ApplicationMtaMetadata applicationMtaMetadata = applicationMtaMetadataExtractor.extractMetadata(application);
        return new ApplicationMtaMetadataEntity(application, applicationMtaMetadata);
    }
}
