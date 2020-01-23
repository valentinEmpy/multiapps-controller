package com.sap.cloud.lm.sl.cf.core.dto.serialization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.mta.model.Version;

@XmlAccessorType(XmlAccessType.FIELD)
public class MtaMetadataDto {

    private String id;
    private String version;

    protected MtaMetadataDto() {
        // Required by JAXB
    }

    public MtaMetadataDto(MtaMetadata metadata) {
        this.id = metadata.getId();
        this.version = metadata.getVersion()
                               .toString();
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

}
