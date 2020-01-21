package com.sap.cloud.lm.sl.cf.core.cf.detect;

import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MtaMetadataEntity;

public interface MtaMetadataExtractorFactory<T extends MtaMetadataEntity> {

    MtaMetadataExtractor<T> get(T metadataEntity);

}