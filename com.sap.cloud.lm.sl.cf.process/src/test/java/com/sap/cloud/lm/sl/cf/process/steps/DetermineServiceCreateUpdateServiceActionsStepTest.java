package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.core.cf.clients.ServiceGetter;
import com.sap.cloud.lm.sl.cf.core.model.ServiceOperation;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.util.ServiceAction;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;

@RunWith(Parameterized.class)
public class DetermineServiceCreateUpdateServiceActionsStepTest
    extends SyncFlowableStepTest<DetermineServiceCreateUpdateServiceActionsStep> {

    @Mock
    private ServiceGetter serviceInstanceGetter;

    private final StepInput stepInput;

    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            {
                "determine-actions-create-or-update-services-step-input-1-create-key.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-2-no-action.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-3-recreate-service.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-4-update-plan.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-5-update-key.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-6-update-tags.json", null,
            },
            {
                "determine-actions-create-or-update-services-step-input-7-update-credentials.json", null,
            },
//          {
//          "determine-actions-create-or-update-services-step-input-8-recreate-service-failure.json", null,
//          },
            {
                "determine-actions-create-or-update-services-step-input-9-recreate-service-error.json", MessageFormat.format(Messages.ERROR_SERVICE_NEEDS_TO_BE_RECREATED_BUT_FLAG_NOT_SET, "service-1", "label-1/plan-3", "service-1", "label-1-old/plan-3"),
            },
            {
                "determine-actions-create-or-update-services-step-input-10-update-credentials.json", null
            },
            {
                "determine-actions-create-or-update-services-step-input-11-no-update-credentials.json", null
            },
            {
                "determine-actions-create-or-update-services-step-input-12-last-operation-failed.json", null
            },
            {
                "determine-actions-create-or-update-services-step-input-13-last-operation-failed-allow-deletion-of-services.json", null
            },
         // @formatter:on
        });
    }

    public DetermineServiceCreateUpdateServiceActionsStepTest(String stepInput, String expectedExceptionMessage) {
        this.stepInput = JsonUtil.fromJson(TestUtil.getResourceAsString(stepInput,
                                                                        DetermineServiceCreateUpdateServiceActionsStepTest.class),
                                           StepInput.class);
        if (expectedExceptionMessage != null) {
            expectedException.expectMessage(expectedExceptionMessage);
        }
    }

    @Before
    public void setUp() {
        prepareContext();
        prepareClient();
        prepareServiceInstanceGetter();
    }

    private void prepareServiceInstanceGetter() {
        Mockito.reset(serviceInstanceGetter);
        Mockito.when(serviceInstanceGetter.getServiceInstanceEntity(any(), any(), any()))
               .thenReturn(stepInput.getExistingServiceInstanceEntity());
    }

    private void prepareContext() {
        context.setVariable(Variables.SERVICE_KEYS_TO_CREATE, stepInput.getServiceKeysToCreate());
        context.setVariable(Variables.SERVICE_TO_PROCESS, stepInput.service);
        context.setVariable(Variables.DELETE_SERVICE_KEYS, true);
        context.setVariable(Variables.DELETE_SERVICES, stepInput.shouldDeleteServices);
    }

    @Test
    public void testExecute() {
        step.execute(execution);

        assertStepIsRunning();

        validateActions();
    }

    private void validateActions() {
        List<ServiceAction> serviceActionsToExecute = context.getVariable(Variables.SERVICE_ACTIONS_TO_EXCECUTE);
        if (stepInput.shouldCreateService) {
            collector.checkThat("Actions should contain " + ServiceAction.CREATE, serviceActionsToExecute.contains(ServiceAction.CREATE),
                                Is.is(true));
        }
        if (stepInput.shouldRecreateService) {
            collector.checkThat("Actions should contain " + ServiceAction.RECREATE,
                                serviceActionsToExecute.contains(ServiceAction.RECREATE), Is.is(true));
        }
        if (stepInput.shouldUpdateServicePlan) {
            collector.checkThat("Actions should contain " + ServiceAction.UPDATE_PLAN,
                                serviceActionsToExecute.contains(ServiceAction.UPDATE_PLAN), Is.is(true));
        }
        if (stepInput.shouldUpdateServiceTags) {
            collector.checkThat("Actions should contain " + ServiceAction.UPDATE_TAGS,
                                serviceActionsToExecute.contains(ServiceAction.UPDATE_TAGS), Is.is(true));
        }
        if (stepInput.shouldUpdateServiceCredentials) {
            collector.checkThat("Actions should contain " + ServiceAction.UPDATE_CREDENTIALS,
                                serviceActionsToExecute.contains(ServiceAction.UPDATE_CREDENTIALS), Is.is(true));
        }
        if (stepInput.shouldUpdateServiceKeys) {
            collector.checkThat("Actions should contain " + ServiceAction.UPDATE_KEYS,
                                serviceActionsToExecute.contains(ServiceAction.UPDATE_KEYS), Is.is(true));
        }
    }

    private void assertStepIsRunning() {
        assertEquals(StepPhase.DONE.toString(), getExecutionStatus());
    }

    private void prepareClient() {
        if (stepInput.existingService != null) {
            Mockito.when(client.getServiceInstance(stepInput.existingService.getName(), false))
                   .thenReturn(stepInput.existingService);
            Mockito.when(client.getServiceInstanceParameters(UUID.fromString("beeb5e8d-4ab9-46ee-9205-455a278743f0")))
                   .thenThrow(new CloudOperationException(HttpStatus.BAD_REQUEST));
            Mockito.when(client.getServiceInstanceParameters(UUID.fromString("400bfc4d-5fce-4a41-bae7-765345e1ce27")))
                   .thenReturn(stepInput.existingService.getCredentials());
        }
    }

    private static class StepInput {

        // ServiceData - Input
        CloudServiceInstanceExtended service;
        CloudServiceInstanceExtended existingService;
        ServiceOperation lastOperationForExistingService;

        // ServiceData - Expectation
        boolean shouldCreateService;
        boolean shouldDeleteServices;
        boolean shouldRecreateService;
        boolean shouldUpdateServicePlan;
        boolean shouldUpdateServiceKeys;
        boolean shouldUpdateServiceTags;
        boolean shouldUpdateServiceCredentials;

        // ServiceKeys - Input
        final List<CloudServiceKey> serviceKeysToCreate = Collections.emptyList();
        // ServiceKeys - Expectation

        public Map<String, List<CloudServiceKey>> getServiceKeysToCreate() {
            Map<String, List<CloudServiceKey>> result = new HashMap<>();
            result.put(service.getName(), serviceKeysToCreate);
            return result;
        }

        public Map<String, Object> getExistingServiceInstanceEntity() {
            if (existingService == null) {
                return null;
            }
            Map<String, Object> result = new HashMap<>();
            if (lastOperationForExistingService != null) {
                Map<String, String> operation = new HashMap<>();
                operation.put("type", lastOperationForExistingService.getType()
                                                                     .toString());
                operation.put("state", lastOperationForExistingService.getState()
                                                                      .toString());
                result.put("last_operation", operation);
            }
            if (existingService.getTags() != null) {
                result.put("tags", existingService.getTags());
            }
            return result;
        }
    }

    @Override
    protected DetermineServiceCreateUpdateServiceActionsStep createStep() {
        return new DetermineServiceCreateUpdateServiceActionsStep();
    }

}
