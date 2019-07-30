package com.sap.cloud.lm.sl.cf.core.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.sap.cloud.lm.sl.cf.core.persistence.query.OperationQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.AbstractQueryImpl.OrderDirection;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.ProcessType;
import com.sap.cloud.lm.sl.cf.web.api.model.State;

public class OperationQueryMock implements OperationQuery {

    @Override
    public OperationQuery limitOnSelect(int limit) {
        return this;
    }

    @Override
    public OperationQuery offsetOnSelect(int offset) {
        return this;
    }

    @Override
    public Operation singleResult() {
        return null;
    }

    @Override
    public Operation singleResultOrNull() {
        return null;
    }

    @Override
    public List<Operation> list() {
        return Collections.emptyList();
    }

    @Override
    public int delete() {
        return 0;
    }

    @Override
    public OperationQuery processId(String processId) {
        return this;
    }

    @Override
    public OperationQuery processType(ProcessType processType) {
        return this;
    }

    @Override
    public OperationQuery spaceId(String spaceId) {
        return this;
    }

    @Override
    public OperationQuery mtaId(String mtaId) {
        return this;
    }

    @Override
    public OperationQuery user(String user) {
        return this;
    }

    @Override
    public OperationQuery acquiredLock(Boolean acquiredLock) {
        return this;
    }

    @Override
    public OperationQuery state(State finalState) {
        return this;
    }

    @Override
    public OperationQuery startedBefore(Date startedBefore) {
        return this;
    }

    @Override
    public OperationQuery endedAfter(Date endedAfter) {
        return this;
    }

    @Override
    public OperationQuery inNonFinalState() {
        return this;
    }

    @Override
    public OperationQuery inFinalState() {
        return this;
    }

    @Override
    public OperationQuery withStateAnyOf(List<State> states) {
        return this;
    }

    @Override
    public OperationQuery orderByProcessId(OrderDirection orderDirection) {
        return this;
    }

    @Override
    public OperationQuery orderByEndTime(OrderDirection orderDirection) {
        return this;
    }

    @Override
    public OperationQuery orderByStartTime(OrderDirection orderDirection) {
        return this;
    }

}
