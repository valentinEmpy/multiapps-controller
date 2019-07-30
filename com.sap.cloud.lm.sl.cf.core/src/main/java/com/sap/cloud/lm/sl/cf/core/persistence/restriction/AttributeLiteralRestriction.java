package com.sap.cloud.lm.sl.cf.core.persistence.restriction;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AttributeLiteralRestriction implements AbstractAttributeRestriction {

    private LiteralCriteria criteria;
    private Object value;

    public static AttributeLiteralRestriction of(LiteralCriteria criteria, Object value) {
        return new AttributeLiteralRestriction(criteria, value);
    }

    public AttributeLiteralRestriction(LiteralCriteria criteria, Object value) {
        this.criteria = criteria;
        this.value = value;
    }

    public Predicate applyOn(Root<?> root, String attributeName) {
        return criteria.satisfiedBy(root.get(attributeName), value);
    }

    public interface LiteralCriteria {

        Predicate satisfiedBy(Expression<?> expression, Object value);
    }
}