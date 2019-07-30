package com.sap.cloud.lm.sl.cf.core.persistence.restriction;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AttributeStringRestriction implements AbstractAttributeRestriction {

    private StringCriteria criteria;
    private String value;

    public static AttributeStringRestriction of(StringCriteria criteria, String value) {
        return new AttributeStringRestriction(criteria, value);
    }

    public AttributeStringRestriction(StringCriteria criteria, String value) {
        this.criteria = criteria;
        this.value = value;
    }

    @Override
    public Predicate applyOn(Root<?> root, String attributeName) {
        return criteria.satisfiedBy(root.get(attributeName), value);
    }

    public interface StringCriteria {
        Predicate satisfiedBy(Expression<String> expression, String object);
    }
}
