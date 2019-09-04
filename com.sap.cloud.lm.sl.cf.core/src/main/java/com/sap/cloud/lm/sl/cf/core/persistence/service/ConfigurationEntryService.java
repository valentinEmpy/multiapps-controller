package com.sap.cloud.lm.sl.cf.core.persistence.service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.ObjectUtils;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.ConfigurationEntryDto;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.ConfigurationEntryFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationEntryQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.ConfigurationEntryQueryImpl;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Named
public class ConfigurationEntryService extends PersistenceService<ConfigurationEntry, ConfigurationEntryDto, Long> {

    @Inject
    protected ConfigurationEntryFactory entryFactory;

    @Inject
    public ConfigurationEntryService(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    public ConfigurationEntryQuery createQuery() {
        return new ConfigurationEntryQueryImpl(createEntityManager(), entryFactory);
    }

    @Override
    protected ConfigurationEntryDto merge(ConfigurationEntryDto existingEntry, ConfigurationEntryDto newEntry) {
        super.merge(existingEntry, newEntry);
        String providerNid = ObjectUtils.firstNonNull(removeDefault(newEntry.getProviderNid()), existingEntry.getProviderNid());
        String providerId = ObjectUtils.firstNonNull(newEntry.getProviderId(), existingEntry.getProviderId());
        String targetOrg = ObjectUtils.firstNonNull(newEntry.getTargetOrg(), existingEntry.getTargetOrg());
        String targetSpace = ObjectUtils.firstNonNull(newEntry.getTargetSpace(), existingEntry.getTargetSpace());
        String providerVersion = ObjectUtils.firstNonNull(removeDefault(newEntry.getProviderVersion()), existingEntry.getProviderVersion());
        String content = ObjectUtils.firstNonNull(newEntry.getContent(), existingEntry.getContent());
        String visibility = ObjectUtils.firstNonNull(newEntry.getVisibility(), existingEntry.getVisibility());
        String spaceId = ObjectUtils.firstNonNull(newEntry.getSpaceId(), existingEntry.getSpaceId());
        return new ConfigurationEntryDto(newEntry.getPrimaryKey(),
                                         providerNid,
                                         providerId,
                                         providerVersion,
                                         targetOrg,
                                         targetSpace,
                                         content,
                                         visibility,
                                         spaceId);
    }

    private String removeDefault(String value) {
        return value.equals(PersistenceMetadata.NOT_AVAILABLE) ? null : value;
    }

    @Override
    protected PersistenceObjectFactory<ConfigurationEntry, ConfigurationEntryDto> getPersistenceObjectFactory() {
        return entryFactory;
    }

    @Override
    protected void onEntityConflict(ConfigurationEntryDto entry, Throwable t) {
        throw (ConflictException) new ConflictException(Messages.CONFIGURATION_ENTRY_ALREADY_EXISTS,
                                                        entry.getProviderNid(),
                                                        entry.getProviderId(),
                                                        entry.getProviderVersion(),
                                                        entry.getTargetOrg(),
                                                        entry.getTargetSpace()).initCause(t);
    }

    @Override
    protected void onEntityNotFound(Long id) {
        throw new NotFoundException(Messages.CONFIGURATION_ENTRY_NOT_FOUND, id);
    }

}