package com.sap.cloud.lm.sl.cf.core.persistence.object.factory;

import com.sap.cloud.lm.sl.cf.core.persistence.dto.DtoWithPrimaryKey;

public abstract class AbstractPersistenceObjectFactory<T, D extends DtoWithPrimaryKey<?>> implements PersistenceObjectFactory<T, D> {

    @Override
    public T fromDto(D dto) {
        return dto != null ? fromNonNullDto(dto) : null;
    }

    @Override
    public D toDto(T object) {
        return object != null ? nonNullObjectToDto(object) : null;
    }
    
    protected abstract T fromNonNullDto(D dto);
    
    protected abstract D nonNullObjectToDto(T object);

}
