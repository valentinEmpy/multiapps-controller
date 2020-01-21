package com.sap.cloud.lm.sl.cf.core.cf.detect.mapping;

import com.sap.cloud.lm.sl.cf.core.cf.detect.metadata.MtaMetadataLabels;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.common.ParsingException;
import org.cloudfoundry.client.v3.Metadata;

import com.sap.cloud.lm.sl.mta.model.Version;

public class MtaMetadataUtil {

    public static String getMtaId(Metadata metadata) {
        if(metadata.getLabels() == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_LABELS);
        }
        return metadata.getLabels().get(MtaMetadataLabels.MTA_ID);
    }

    public static Version getMtaVersion(Metadata metadata) {
        if(metadata.getLabels() == null) {
            throw new ParsingException(Messages.CANT_PARSE_MTA_METADATA_LABELS);
        }
        String version = metadata.getLabels().get(MtaMetadataLabels.MTA_VERSION);
        return version == null ? null : Version.parseVersion(version);
    }
}
