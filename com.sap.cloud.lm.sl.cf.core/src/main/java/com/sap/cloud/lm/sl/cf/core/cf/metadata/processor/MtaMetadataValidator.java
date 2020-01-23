/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sap.cloud.lm.sl.cf.core.cf.metadata.processor;

import javax.inject.Named;

import org.cloudfoundry.client.lib.domain.CloudEntity;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.util.MtaMetadataUtil;
import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.common.ContentException;

@Named
public class MtaMetadataValidator {

    public void validate(CloudEntity entity) {
        validateMtaMetadataLabelsArePresent(entity);
        validateMtaMetadataAnnotationIsPresent(entity);
    }

    private void validateMtaMetadataLabelsArePresent(CloudEntity entity) {
        if (!entity.getV3Metadata()
                   .getLabels()
                   .keySet()
                   .containsAll(MtaMetadataUtil.MTA_METADATA_LABELS)) {
            throw new ContentException(Messages.MTA_METADATA_FOR_0_IS_INCOMPLETE, entity.getName());
        }
    }

    private void validateMtaMetadataAnnotationIsPresent(CloudEntity entity) {
        if (!containsMtaMetadataAnnotation(entity)) {
            throw new ContentException(Messages.MTA_METADATA_FOR_0_IS_INCOMPLETE, entity.getName());
        }
    }

    private boolean containsMtaMetadataAnnotation(CloudEntity entity) {
        return entity.getV3Metadata()
                     .getAnnotations()
                     .keySet()
                     .stream()
                     .anyMatch(MtaMetadataUtil.MTA_METADATA_ANNOTATIONS::contains);
    }
}
