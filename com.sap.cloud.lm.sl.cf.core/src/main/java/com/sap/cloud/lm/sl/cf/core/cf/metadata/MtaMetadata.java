package com.sap.cloud.lm.sl.cf.core.cf.metadata;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionJsonDeserializer;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionJsonSerializer;
import com.sap.cloud.lm.sl.cf.core.model.adapter.VersionXmlAdapter;
import com.sap.cloud.lm.sl.mta.model.Version;

@Value.Immutable
@JsonDeserialize(builder = ImmutableMtaMetadata.Builder.class)
public interface MtaMetadata {

    // In order to keep backwards compatibility the version element cannot be null, since old clients might throw a NPE. TODO: Remove this
    // when compatibility with versions lower than 1.27.3 is not required.
    Version UNKNOWN_MTA_VERSION = Version.parseVersion("0.0.0-unknown");

    String getId();

    @JsonSerialize(using = VersionJsonSerializer.class)
    @JsonDeserialize(using = VersionJsonDeserializer.class)
    @XmlJavaTypeAdapter(VersionXmlAdapter.class)
    Version getVersion();

    default boolean isVersionUnknown() {
        return UNKNOWN_MTA_VERSION.equals(getVersion());
    }

}
