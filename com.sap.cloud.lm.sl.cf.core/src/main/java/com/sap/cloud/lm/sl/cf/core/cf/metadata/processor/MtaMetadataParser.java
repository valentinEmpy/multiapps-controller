package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import static com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil.getMtaId;
import static com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil.getMtaVersion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.v3.Metadata;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.common.ParsingException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Component
public class MtaMetadataParser {

    private static final List<String> METADATA_LABELS = Arrays.asList(MtaMetadataLabels.MTA_ID, MtaMetadataLabels.MTA_VERSION);

    public boolean hasMtaMetadata(CloudEntity entity) {
        Metadata metadata = entity.getV3Metadata();
        if (metadata == null || metadata.getLabels() == null) {
            return false;
        }
        return Collections.disjoint(metadata.getLabels()
                                            .keySet(),
                                    METADATA_LABELS);
    }

    public MtaMetadata parseMtaMetadata(CloudEntity entity) {
        try {
            if (!isMtaMetadataComplete(entity)) {
                throw new ParsingException(Messages.MTA_METADATA_FOR_0_IS_INCOMPLETE, entity.getName());
            }
            return toMtaMetadata(entity.getV3Metadata());
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_FOR_0, entity.getName());
        }
    }

    private boolean isMtaMetadataComplete(CloudEntity entity) {
        return entity.getV3Metadata()
                     .getLabels()
                     .keySet()
                     .containsAll(METADATA_LABELS);
    }

    private MtaMetadata toMtaMetadata(Metadata metadata) {
        return ImmutableMtaMetadata.builder()
                                   .id(getMtaId(metadata))
                                   .version(getMtaVersion(metadata))
                                   .build();
    }

    public DeployedMtaModule parseModule(CloudApplication application) {
        try {
            if (!isMtaMetadataComplete(application)) {
                throw new ParsingException(Messages.MTA_METADATA_FOR_APP_0_IS_INCOMPLETE, application.getName());
            }
            String moduleJson = application.getV3Metadata()
                                           .getAnnotations()
                                           .get(MtaMetadataAnnotations.MODULE);
            if (moduleJson == null) {
                throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
            }
            return JsonUtil.fromJson(moduleJson, DeployedMtaModule.class);
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_FOR_0, application.getName());
        }
    }

    public DeployedMtaResource parseResource(CloudService service) {
        try {
            if (!isMtaMetadataComplete(service)) {
                throw new ParsingException(Messages.MTA_METADATA_FOR_SERVICE_0_IS_INCOMPLETE, service.getName());
            }
            String resourceJson = service.getV3Metadata()
                                         .getAnnotations()
                                         .get(MtaMetadataAnnotations.RESOURCE);
            if (resourceJson == null) {
                throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_ANNOTATIONS);
            }
            return JsonUtil.fromJson(resourceJson, DeployedMtaResource.class);
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_FOR_0, service.getName());
        }
    }

}
