package com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.processor;

import java.util.List;

import org.cloudfoundry.client.lib.CloudControllerClient;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.entity.MtaMetadataEntity;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.criteria.MtaMetadataCriteria;

public interface MtaMetadataEntityCollector<T extends MtaMetadataEntity> {

    List<T> collect(MtaMetadataCriteria criteria, CloudControllerClient client);

}
