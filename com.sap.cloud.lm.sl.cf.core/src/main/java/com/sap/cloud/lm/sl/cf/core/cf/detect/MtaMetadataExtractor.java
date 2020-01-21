package com.sap.cloud.lm.sl.cf.core.cf.detect;

import java.util.ArrayList;

import com.sap.cloud.lm.sl.cf.core.cf.detect.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;

public interface MtaMetadataExtractor<T extends MtaMetadataEntity> {

    void extract(T entity, DeployedMta metadata);
    
    default void initMetadata(T entity, DeployedMta metadata) {
        if(metadata.getResources() == null) {
            metadata.setResources(new ArrayList<>());
        }
        if(metadata.getModules() == null) {
            metadata.setModules(new ArrayList<>());
        }
        if(metadata.getMetadata() == null && entity.getMtaMetadata() != null) {
            metadata.setMetadata(entity.getMtaMetadata());
        }
    }
}
