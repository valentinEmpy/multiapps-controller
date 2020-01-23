package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import java.text.MessageFormat;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.ImmutableMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataAnnotations;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.common.ParsingException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.Version;

@Named
public class MtaMetadataParser {

    private MtaMetadataValidator mtaMetadataValidator;

    @Inject
    public MtaMetadataParser(MtaMetadataValidator mtaMetadataValidator) {
        this.mtaMetadataValidator = mtaMetadataValidator;
    }

    public MtaMetadata parseMtaMetadata(CloudEntity entity) {
        mtaMetadataValidator.validate(entity);
        String mtaId = entity.getV3Metadata()
                             .getLabels()
                             .get(MtaMetadataLabels.MTA_ID);
        Version mtaVersion = parseMtaVersion(entity);
        return ImmutableMtaMetadata.builder()
                                   .id(mtaId)
                                   .version(mtaVersion)
                                   .build();
    }

    private Version parseMtaVersion(CloudEntity entity) {
        try {
            String version = entity.getV3Metadata()
                                   .getLabels()
                                   .get(MtaMetadataLabels.MTA_VERSION);
            return version == null ? null : Version.parseVersion(version);
        } catch (ParsingException e) {
            throw new ParsingException(e, Messages.CANT_PARSE_MTA_METADATA_VERSION_FOR_0, entity.getName());
        }
    }

    public DeployedMtaModule parseModule(CloudApplication application) {
        mtaMetadataValidator.validate(application);
        String moduleJson = application.getV3Metadata()
                                       .getAnnotations()
                                       .get(MtaMetadataAnnotations.MODULE);
        String messageOnParsingException = getMessageOnParsingException(application);
        return parse(moduleJson, DeployedMtaModule.class, messageOnParsingException);
    }

    public DeployedMtaResource parseResource(CloudService service) {
        mtaMetadataValidator.validate(service);
        String resourceJson = service.getV3Metadata()
                                     .getAnnotations()
                                     .get(MtaMetadataAnnotations.RESOURCE);
        String messageOnParsingException = getMessageOnParsingException(service);
        return parse(resourceJson, DeployedMtaResource.class, messageOnParsingException);
    }

    private String getMessageOnParsingException(CloudEntity entity) {
        return MessageFormat.format(Messages.CANT_PARSE_MTA_METADATA_FOR_0, entity.getName());
    }

    private <T> T parse(String json, Class<T> tClass, String exceptionMessage) {
        try {
            return JsonUtil.fromJson(json, tClass);
        } catch (ParsingException e) {
            throw new ParsingException(e, exceptionMessage);
        }
    }

}
