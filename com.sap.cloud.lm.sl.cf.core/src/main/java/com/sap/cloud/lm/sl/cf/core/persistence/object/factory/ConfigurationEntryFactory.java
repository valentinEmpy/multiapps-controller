package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationEntryDto;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.mta.model.Version;

@Component
public class ConfigurationEntryFactory extends AbstractPersistenceObjectFactory<ConfigurationEntry, ConfigurationEntryDto> {

    @Override
    protected ConfigurationEntry fromNonNullDto(ConfigurationEntryDto dto) {
        Long id = dto.getPrimaryKey();
        String providerNid = getOriginal(dto.getProviderNid());
        String providerId = dto.getProviderId();
        Version version = getParsedVersion(getOriginal(dto.getProviderVersion()));
        CloudTarget targetSpace = new CloudTarget(dto.getTargetOrg(), dto.getTargetSpace());
        String content = dto.getContent();
        List<CloudTarget> visibility = getParsedVisibility(dto.getVisibility());
        String spaceId = dto.getSpaceId();
        return new ConfigurationEntry(id, providerNid, providerId, version, targetSpace, content, visibility, spaceId);
    }

    private String getOriginal(String source) {
        if (source == null || source.equals(PersistenceMetadata.NOT_AVAILABLE)) {
            return null;
        }
        return source;
    }

    private Version getParsedVersion(String versionString) {
        if (versionString == null) {
            return null;
        }
        return Version.parseVersion(versionString);
    }

    private List<CloudTarget> getParsedVisibility(String visibility) {
        return visibility == null ? null : JsonUtil.convertJsonToList(visibility, new TypeReference<List<CloudTarget>>() {
        });
    }

    @Override
    protected ConfigurationEntryDto nonNullObjectToDto(ConfigurationEntry entry) {
        long id = entry.getId();
        String providerNid = getNotNull(entry.getProviderNid());
        String providerId = entry.getProviderId();
        String providerVersion = getNotNull(entry.getProviderVersion());
        String targetOrg = entry.getTargetSpace() == null ? null
            : entry.getTargetSpace()
                .getOrg();
        String targetSpace = entry.getTargetSpace() == null ? null
            : entry.getTargetSpace()
                .getSpace();
        String content = entry.getContent();
        String visibility = entry.getVisibility() == null ? null : JsonUtil.toJson(entry.getVisibility());
        String spaceId = entry.getSpaceId();
        return new ConfigurationEntryDto(id, providerNid, providerId, providerVersion, targetOrg, targetSpace, content, visibility,
            spaceId);
    }

    private String getNotNull(Object source) {
        if (source == null) {
            return PersistenceMetadata.NOT_AVAILABLE;
        }
        return source.toString();
    }

}
