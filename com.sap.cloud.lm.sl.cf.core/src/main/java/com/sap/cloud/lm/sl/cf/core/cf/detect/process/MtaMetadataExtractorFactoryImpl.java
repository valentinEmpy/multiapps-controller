package com.sap.cloud.lm.sl.cf.core.cf.detect.process;

import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataExtractor;
import com.sap.cloud.lm.sl.cf.core.cf.detect.MtaMetadataExtractorFactory;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ApplicationMtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.ServiceMtaMetadataEntity;

@Component
public class MtaMetadataExtractorFactoryImpl<T extends MtaMetadataEntity> implements MtaMetadataExtractorFactory<T> {

    @Override
    public MtaMetadataExtractor<T> get(T metadataEntity) {
        if(metadataEntity instanceof ApplicationMtaMetadataEntity) {
            return (MtaMetadataExtractor<T>) new AppMtaMetadataExtractor();
        }
        if(metadataEntity instanceof ServiceMtaMetadataEntity) {
            return (MtaMetadataExtractor<T>) new ServiceMtaMetadataExtractor();
        }
        return null;
    }
    
}
