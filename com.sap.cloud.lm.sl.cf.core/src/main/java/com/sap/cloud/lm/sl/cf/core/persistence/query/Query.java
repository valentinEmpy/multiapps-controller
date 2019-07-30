package com.sap.cloud.lm.sl.cf.core.persistence.query;

import java.util.List;

public interface Query<R, T extends Query<?, ?>> {

    T limitOnSelect(int limit);

    T offsetOnSelect(int offset);

    R singleResult();

    R singleResultOrNull();

    List<R> list();

    int delete();
}
