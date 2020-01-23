package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import static com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil.getMtaId;
import static com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil.getMtaVersion;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.v3.Metadata;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.common.ParsingException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Component
public class MtaMetadataParser {

    public MtaMetadata parseMtaMetadata(CloudEntity entity) {
        Metadata metadata = entity.getV3Metadata();
        if (metadata == null) {
            return null;
        }
        return tryParseMetadata(entity, metadata);
    }

    private MtaMetadata tryParseMetadata(CloudEntity entity, Metadata metadata) {
        try {
            return ImmutableMtaMetadata.builder()
                                       .id(getMtaId(metadata))
                                       .version(getMtaVersion(metadata))
                                       .build();
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_FOR_0, entity.getName());
        }
    }

    public DeployedMtaModule parseModule(CloudApplication application) {
        if (!canParseMetadataAnnotations(application)) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        String moduleJson = application.getV3Metadata()
                                       .getAnnotations()
                                       .get(MtaMetadataAnnotations.MODULE);
        if (moduleJson == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        return JsonUtil.fromJson(moduleJson, DeployedMtaModule.class);
    }

    private boolean canParseMetadataAnnotations(CloudEntity entity) {
        return entity.getV3Metadata() != null && entity.getV3Metadata()
                                                       .getAnnotations() != null;
    }

    public DeployedMtaResource parseResource(CloudService service) {
        if (!canParseMetadataAnnotations(service)) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        String resourceJson = service.getV3Metadata()
                                     .getAnnotations()
                                     .get(MtaMetadataAnnotations.RESOURCE);
        if (resourceJson == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
        }
        return JsonUtil.fromJson(resourceJson, DeployedMtaResource.class);
    }

}
