package com.sap.cloud.lm.sl.cf.process.steps;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.persistence.query.ConfigurationSubscriptionQuery;
import com.sap.cloud.lm.sl.cf.core.persistence.query.impl.ConfigurationSubscriptionQueryImpl;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ConfigurationSubscriptionService;
import com.sap.cloud.lm.sl.common.NotFoundException;

@RunWith(Parameterized.class)
public class DeleteSubscriptionsStepTest extends SyncFlowableStepTest<DeleteSubscriptionsStep> {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            // (0)
            {
                new StepInput(Arrays.asList(1l, 2l, 3l),  Arrays.asList(1l, 2l, 3l)), null,
            },
            // (1)
            {
                new StepInput(Arrays.asList(1l, 2l, 3l),  Arrays.asList(1l, 2l, 3l, 4l, 5l, 6l)), null,
            },
            // (2)
            {
               new StepInput(Collections.emptyList(),  Arrays.asList(1l, 2l, 3l)), null,
            },
            // (3) A NotFoundException should not be thrown if the subscriptions were already deleted:
            {
                new StepInput(Arrays.asList(1l, 2l, 3l), Collections.emptyList()), null,
            },
// @formatter:on
        });
    }

    @Mock
    private ConfigurationSubscriptionService service;
    @Mock
    private ConfigurationSubscriptionQuery query = Mockito.mock(ConfigurationSubscriptionQueryImpl.class);

    private final String expectedExceptionMessage;
    private final StepInput input;

    public DeleteSubscriptionsStepTest(StepInput input, String expectedExceptionMessage) {
        this.expectedExceptionMessage = expectedExceptionMessage;
        this.input = input;
    }

    @Before
    public void setUp() {
        loadParameters();
        prepareContext();
        prepareDao();
    }

    private void prepareContext() {
        StepsUtil.setSubscriptionsToDelete(context, asSubscriptions(input.subscriptionsToDelete));
    }

    private List<ConfigurationSubscription> asSubscriptions(List<Long> subscriptionsToDelete) {
        return subscriptionsToDelete.stream()
            .map(this::asSubscription)
            .collect(Collectors.toList());
    }

    private ConfigurationSubscription asSubscription(Long subscriptionnId) {
        return new ConfigurationSubscription(subscriptionnId, null, null, null, null, null, null);
    }

    private void loadParameters() {
        if (expectedExceptionMessage != null) {
            expectedException.expectMessage(expectedExceptionMessage);
        }
    }

    private void prepareDao() {
        List<Long> nonExistingSubscriptions = new ArrayList<>(input.subscriptionsToDelete);
        nonExistingSubscriptions.removeAll(input.existingSubscriptions);
        doReturn(query).when(service)
            .createQuery();
        for (Long subscription : nonExistingSubscriptions) {
            ConfigurationSubscriptionQuery nonExistingSubscriptionQuery = Mockito.mock(ConfigurationSubscriptionQueryImpl.class);
            doThrow(new NotFoundException(Messages.CONFIGURATION_SUBSCRIPTION_NOT_FOUND, subscription)).when(nonExistingSubscriptionQuery)
                .delete();
            doReturn(nonExistingSubscriptionQuery).when(query)
                .id(subscription);
        }
    }

    @Test
    public void testExecute() {
        List<ConfigurationSubscriptionQuery> deleteQueries = new ArrayList<>();
        List<ConfigurationSubscriptionQuery> nonExecutedDeleteQueries = new ArrayList<>();
        for (Long subscription : input.existingSubscriptions) {
            ConfigurationSubscriptionQuery mock = Mockito.mock(ConfigurationSubscriptionQueryImpl.class);
            doReturn(mock).when(query)
                .id(subscription);
            if (input.subscriptionsToDelete.contains(subscription)) {
                deleteQueries.add(mock);
            } else {
                nonExecutedDeleteQueries.add(mock);
            }
        }

        step.execute(context);

        assertStepFinishedSuccessfully();

        for (ConfigurationSubscriptionQuery deleteQuery : deleteQueries) {
            Mockito.verify(deleteQuery, times(1))
                .delete();
        }
        for (ConfigurationSubscriptionQuery nonExecutedDeleteQuery : nonExecutedDeleteQueries) {
            Mockito.verify(nonExecutedDeleteQuery, times(0))
                .delete();
        }
    }

    private static class StepInput {

        public List<Long> subscriptionsToDelete;
        public List<Long> existingSubscriptions;

        public StepInput(List<Long> subscriptionsToDelete, List<Long> existingSubscriptions) {
            this.subscriptionsToDelete = subscriptionsToDelete;
            this.existingSubscriptions = existingSubscriptions;
        }

    }

    @Override
    protected DeleteSubscriptionsStep createStep() {
        return new DeleteSubscriptionsStep();
    }

}
