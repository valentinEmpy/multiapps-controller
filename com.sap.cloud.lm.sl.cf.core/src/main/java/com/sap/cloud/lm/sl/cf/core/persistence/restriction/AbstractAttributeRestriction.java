package com.sap.cloud.lm.sl.cf.core.persistence.restriction;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface AbstractAttributeRestriction {

    Predicate applyOn(Root<?> root, String attributeName);

}
