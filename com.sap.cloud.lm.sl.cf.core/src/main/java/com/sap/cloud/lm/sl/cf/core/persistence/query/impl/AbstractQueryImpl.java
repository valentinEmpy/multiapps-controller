package com.sap.cloud.lm.sl.cf.core.persistence.query.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.sap.cloud.lm.sl.cf.core.persistence.TransactionalExecutor;
import com.sap.cloud.lm.sl.cf.core.persistence.query.Query;
import com.sap.cloud.lm.sl.cf.core.persistence.restriction.AbstractAttributeRestriction;

public abstract class AbstractQueryImpl<R, T extends Query<R, T>> implements Query<R, T> {

    public enum OrderDirection {
        ASCENDING, DESCENDING
    }

    private Integer limit;
    private Integer offset;
    private OrderDirection orderDirection;
    private String orderAttribute;

    @Override
    public T limitOnSelect(int limit) {
        this.limit = limit;
        return getSelf();
    }

    @Override
    public T offsetOnSelect(int offset) {
        this.offset = offset;
        return getSelf();
    }

    protected T setOrder(String orderAttribute, OrderDirection orderDirection) {
        this.orderAttribute = orderAttribute;
        this.orderDirection = orderDirection;
        return getSelf();
    }

    private <E> void applyLimitAndOffset(TypedQuery<E> typedQuery) {
        if (limit != null) {
            typedQuery.setMaxResults(limit);
        }
        if (offset != null) {
            typedQuery.setFirstResult(offset);
        }
    }

    private <E> CriteriaQuery<E> applyOrder(CriteriaQuery<E> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<E> root) {
        if (orderAttribute == null) {
            return criteriaQuery;
        }
        if (orderDirection == OrderDirection.ASCENDING) {
            return criteriaQuery.orderBy(criteriaBuilder.asc(root.get(orderAttribute)));
        }
        return criteriaQuery.orderBy(criteriaBuilder.desc(root.get(orderAttribute)));
    }

    protected <E> TypedQuery<E> createQuery(EntityManager entityManager, CriteriaBuilder criteriaBuilder,
        Map<String, AbstractAttributeRestriction> attributeRestrictions, Class<E> dtoClass) {
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(dtoClass);
        Root<E> root = criteriaQuery.from(dtoClass);
        criteriaQuery.where(getQueryPredicates(root, attributeRestrictions).toArray(new Predicate[0]));
        criteriaQuery = applyOrder(criteriaQuery, criteriaBuilder, root);
        TypedQuery<E> typedQuery = entityManager.createQuery(criteriaQuery);
        applyLimitAndOffset(typedQuery);
        return typedQuery;
    }

    protected <E> javax.persistence.Query createDeleteQuery(EntityManager entityManager, CriteriaBuilder criteriaBuilder,
        Map<String, AbstractAttributeRestriction> attributeRestrictions, Class<E> dtoClass) {
        CriteriaDelete<E> deleteQuery = criteriaBuilder.createCriteriaDelete(dtoClass);
        Root<E> root = deleteQuery.from(dtoClass);
        deleteQuery.where(getQueryPredicates(root, attributeRestrictions).toArray(new Predicate[0]));
        return entityManager.createQuery(deleteQuery);
    }

    protected <E> E executeInTransaction(EntityManager entityManager, Function<EntityManager, E> function) {
        return new TransactionalExecutor<E>(entityManager).execute(function);
    }

    private <E> List<Predicate> getQueryPredicates(Root<E> root, Map<String, AbstractAttributeRestriction> attributeRestrictions) {
        return attributeRestrictions.entrySet()
            .stream()
            .map(restriction -> toPredicate(restriction, root))
            .collect(Collectors.toList());
    }

    private <E> Predicate toPredicate(Entry<String, AbstractAttributeRestriction> attributeRestriction, Root<E> root) {
        return attributeRestriction.getValue()
            .applyOn(root, attributeRestriction.getKey());
    }

    @Override
    public R singleResultOrNull() {
        try {
            return singleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public T getSelf() {
        return (T) this;
    }
}
