package com.sap.cloud.lm.sl.cf.core.cf.detect.process;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataEntityCollector;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ApplicationMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.mapping.ApplicationMtaMetadataExtractor;
import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.criteria.MtaMetadataCriteria;
import com.sap.cloud.lm.sl.cf.core.model.ApplicationMtaMetadata;

@Component
public class ApplicationMtaMetadataEntityCollector implements MtaMetadataEntityCollector<ApplicationMtaMetadataEntity> {

    @Inject
    private ApplicationMtaMetadataExtractor applicationMtaMetadataExtractor;

    @Override
    public List<ApplicationMtaMetadataEntity> collect(MtaMetadataCriteria criteria, CloudControllerClient client) {
        return client.getApplicationsByMetadataLabelSelector(criteria.get())
                     .stream()
                     .map(this::extractMetadata)
                     .collect(Collectors.toList());
    }

    private ApplicationMtaMetadataEntity extractMetadata(CloudApplication application) {
        ApplicationMtaMetadata applicationMtaMetadata = applicationMtaMetadataExtractor.extractMetadata(application);
        return new ApplicationMtaMetadataEntity(application, applicationMtaMetadata);
    }
}
