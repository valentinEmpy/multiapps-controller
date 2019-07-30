package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import com.sap.cloud.lm.sl.cf.core.persistence.dto.DtoWithPrimaryKey;

public interface PersistenceObjectFactory<T, D extends DtoWithPrimaryKey<?>> {

    T fromDto(D dto);

    D toDto(T object);

}
