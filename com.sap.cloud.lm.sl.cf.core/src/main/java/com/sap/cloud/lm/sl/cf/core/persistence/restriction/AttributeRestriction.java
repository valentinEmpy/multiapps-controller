package com.sap.cloud.lm.sl.cf.core.persistence.restriction;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AttributeRestriction<T extends Comparable<? super T>> implements AbstractAttributeRestriction {

    private Criteria<T> criteria;
    private T value;

    public static <T extends Comparable<? super T>> AttributeRestriction<T> of(Criteria<T> criteria, T value) {
        return new AttributeRestriction<>(criteria, value);
    }

    public AttributeRestriction(Criteria<T> criteria, T value) {
        this.criteria = criteria;
        this.value = value;
    }

    @Override
    public Predicate applyOn(Root<?> root, String attributeName) {
        return criteria.satisfiedBy(root.get(attributeName), value);
    }

    public interface Criteria<T extends Comparable<? super T>> {

        Predicate satisfiedBy(Expression<? extends T> expression, T object);
    }
}
