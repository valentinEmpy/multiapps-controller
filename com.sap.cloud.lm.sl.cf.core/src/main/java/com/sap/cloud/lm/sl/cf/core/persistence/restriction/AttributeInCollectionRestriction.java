package com.sap.cloud.lm.sl.cf.core.persistence.restriction;

import java.util.Collection;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class AttributeInCollectionRestriction implements AbstractAttributeRestriction {

    private Collection<?> collection;

    public static AttributeInCollectionRestriction of(Collection<?> collection) {
        return new AttributeInCollectionRestriction(collection);
    }

    public AttributeInCollectionRestriction(Collection<?> collection) {
        this.collection = collection;
    }

    @Override
    public Predicate applyOn(Root<?> root, String attributeName) {
        return root.get(attributeName)
            .in(collection);
    }

}
