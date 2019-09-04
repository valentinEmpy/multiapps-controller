package com.sap.cloud.lm.sl.cf.core.persistence.service;

import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.RollbackException;

import com.sap.cloud.lm.sl.cf.core.persistence.TransactionalExecutor;
import com.sap.cloud.lm.sl.cf.core.persistence.dto.DtoWithPrimaryKey;
import com.sap.cloud.lm.sl.cf.core.persistence.object.factory.PersistenceObjectFactory;

public abstract class PersistenceService<T, D extends DtoWithPrimaryKey<P>, P> {

    private EntityManagerFactory entityManagerFactory;

    public PersistenceService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void add(T object) {
        D dto = getPersistenceObjectFactory().toDto(object);
        try {
            executeInTransaction(manager -> {
                manager.persist(dto);
                return null;
            });
        } catch (RollbackException e) {
            onEntityConflict(dto, e);
        }
    }

    private <R> R executeInTransaction(Function<EntityManager, R> function) {
        return new TransactionalExecutor<R>(createEntityManager()).execute(function);
    }

    protected EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public T update(P primaryKey, T newObject) {
        D newDto = getPersistenceObjectFactory().toDto(newObject);
        try {
            return executeInTransaction(manager -> {
                @SuppressWarnings("unchecked")
                D existingDto = manager.find((Class<D>) newDto.getClass(), primaryKey);
                if (existingDto == null) {
                    onEntityNotFound(primaryKey);
                }
                D dto = merge(existingDto, newDto);
                manager.merge(dto);
                return getPersistenceObjectFactory().fromDto(dto);
            });
        } catch (RollbackException e) {
            onEntityConflict(newDto, e);
        }
        return null;
    }

    protected D merge(D existingPersistenceObject, D newPersistenceObject) {
        newPersistenceObject.setPrimaryKey(existingPersistenceObject.getPrimaryKey());
        return newPersistenceObject;
    }

    protected abstract PersistenceObjectFactory<T, D> getPersistenceObjectFactory();

    protected abstract void onEntityConflict(D dto, Throwable t);

    protected abstract void onEntityNotFound(P primaryKey);

}