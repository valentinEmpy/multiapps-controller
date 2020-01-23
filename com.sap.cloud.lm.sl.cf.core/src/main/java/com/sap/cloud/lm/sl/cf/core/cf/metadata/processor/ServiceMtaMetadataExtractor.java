package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableServiceMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.common.ParsingException;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.v3.Metadata;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.ServiceMtaMetadata;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Component
public class ServiceMtaMetadataExtractor {

    public static final String RESOURCE = "resource";

    public ServiceMtaMetadata extractMetadata(CloudService service) {
        if (service.getV3Metadata() == null) {
            return null;
        }

        try {
            MtaMetadata mtaMetadata = new MtaMetadata();
            mtaMetadata.setId(MtaMetadataUtil.getMtaId(service.getV3Metadata()));
            mtaMetadata.setVersion(MtaMetadataUtil.getMtaVersion(service.getV3Metadata()));

            DeployedMtaResource resource = getResource(service.getV3Metadata());

            return ImmutableServiceMtaMetadata.builder()
                                     .deployedMtaResource(resource)
                                     .mtaMetadata(mtaMetadata)
                                     .build();
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_FOR_SERVICE_0, service.getName());
        }
    }

    private DeployedMtaResource getResource(Metadata metadata) {
        if (metadata.getAnnotations() == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        String resourceJson = metadata.getAnnotations()
                                      .get(RESOURCE);
        if (resourceJson == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        return JsonUtil.fromJson(resourceJson, new TypeReference<DeployedMtaResource>() {
        });
    }


}
