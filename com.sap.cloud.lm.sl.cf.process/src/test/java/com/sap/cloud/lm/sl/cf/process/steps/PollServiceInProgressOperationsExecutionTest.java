package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.ImmutableCloudMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.core.cf.clients.EventsGetter;
import com.sap.cloud.lm.sl.cf.core.model.ServiceOperation;
import com.sap.cloud.lm.sl.cf.process.util.ServiceOperationGetter;
import com.sap.cloud.lm.sl.cf.process.util.ServiceProgressReporter;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;

@RunWith(Parameterized.class)
public class PollServiceInProgressOperationsExecutionTest extends AsyncStepOperationTest<CheckForOperationsInProgressStep> {

    private static final String TEST_SPACE_ID = "test";
    private static final String TEST_PROVIDER = "testProvider";
    private static final String TEST_PLAN = "testPlan";
    private static final String TEST_VERSION = "0.0.1-beta";

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
            // (0) With 2 services in progress:
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(ServiceOperation.Type.DELETE, ServiceOperation.Type.CREATE, ServiceOperation.Type.DELETE),
                Arrays.asList(ServiceOperation.State.IN_PROGRESS, ServiceOperation.State.SUCCEEDED, ServiceOperation.State.IN_PROGRESS), 
                false, AsyncExecutionState.RUNNING, null
            },
            // (1) With 1 service in progress state and 1 successfully deleted
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(ServiceOperation.Type.DELETE, ServiceOperation.Type.CREATE, ServiceOperation.Type.DELETE),
                Arrays.asList(null, ServiceOperation.State.SUCCEEDED, ServiceOperation.State.IN_PROGRESS), 
                true, AsyncExecutionState.RUNNING, null
            },
            // (2) With 3 services finished operations successfully 
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(ServiceOperation.Type.UPDATE, ServiceOperation.Type.CREATE, ServiceOperation.Type.DELETE),
                Arrays.asList(ServiceOperation.State.SUCCEEDED, ServiceOperation.State.SUCCEEDED, ServiceOperation.State.SUCCEEDED), 
                false, AsyncExecutionState.FINISHED, null
            },
            // (3) Handle missing response for last service operation
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(null, ServiceOperation.Type.CREATE, null),
                Arrays.asList(null, null, null), 
                false, AsyncExecutionState.FINISHED, null
            },
            // (4) Throw exception on create failed service state
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(null, ServiceOperation.Type.CREATE, null),
                Arrays.asList(null, ServiceOperation.State.FAILED, null), 
                true, null, "Error creating service \"service2\" from offering \"null\" and plan \"testPlan\""
            },
            // (5) Throw exception on update failed service state
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(null, ServiceOperation.Type.UPDATE, null),
                Arrays.asList(null, ServiceOperation.State.FAILED, null), 
                true, null, "Error updating service \"service2\" from offering \"null\" and plan \"testPlan\""
            },
            // (5) Throw exception on delete failed service state
            {
                Arrays.asList("service1","service2", "service3"), 
                Arrays.asList(null, ServiceOperation.Type.DELETE, null),
                Arrays.asList(null, ServiceOperation.State.FAILED, null), 
                true, null, "Error deleting service \"service2\" from offering \"null\" and plan \"testPlan\""
            },            
// @formatter:on
        });
    }

    public PollServiceInProgressOperationsExecutionTest(List<String> serviceNames, List<ServiceOperation.Type> servicesOperationTypes,
                                                        List<ServiceOperation.State> servicesOperationStates,
                                                        boolean shouldVerifyStepLogger, AsyncExecutionState expectedExecutionState,
                                                        String expectedExceptionMessage) {
        this.serviceNames = serviceNames;
        this.servicesOperationTypes = servicesOperationTypes;
        this.servicesOperationStates = servicesOperationStates;
        this.shouldVerifyStepLogger = shouldVerifyStepLogger;
        this.expectedExecutionState = expectedExecutionState;
        this.expectedExceptionMessage = expectedExceptionMessage;
    }

    @Mock
    private ServiceOperationGetter serviceOperationGetter;
    @Mock
    private ServiceProgressReporter serviceProgressReporter;
    @Mock
    private EventsGetter eventsGetter;
    @Mock
    private CloudControllerClient client;
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private final List<String> serviceNames;
    private final List<ServiceOperation.Type> servicesOperationTypes;
    private final List<ServiceOperation.State> servicesOperationStates;
    private final boolean shouldVerifyStepLogger;
    private final AsyncExecutionState expectedExecutionState;
    private final String expectedExceptionMessage;

    @Before
    public void setUp() {
        context.setVariable(Variables.SPACE_ID, TEST_SPACE_ID);
        List<CloudServiceInstanceExtended> services = generateCloudServicesExtended();
        prepareServiceOperationGetter(services);
        prepareServicesData(services);
        prepareTriggeredServiceOperations();
        when(clientProvider.getControllerClient(anyString(), anyString())).thenReturn(client);
        if (expectedExceptionMessage != null) {
            exception.expectMessage(expectedExceptionMessage);
        }
    }

    private void prepareServiceOperationGetter(List<CloudServiceInstanceExtended> services) {
        for (int i = 0; i < services.size(); i++) {
            CloudServiceInstanceExtended service = services.get(i);
            ServiceOperation.Type serviceOperationType = servicesOperationTypes.get(i);
            ServiceOperation.State serviceOperationState = servicesOperationStates.get(i);
            if (serviceOperationType != null && serviceOperationState != null) {
                when(serviceOperationGetter.getLastServiceOperation(any(),
                                                                    eq(service))).thenReturn(new ServiceOperation(serviceOperationType,
                                                                                                                  "",
                                                                                                                  serviceOperationState));
            }
        }
    }

    private void prepareTriggeredServiceOperations() {
        Map<String, ServiceOperation.Type> triggeredServiceOperations = new HashMap<>();
        for (int index = 0; index < serviceNames.size(); index++) {
            String serviceName = serviceNames.get(index);
            ServiceOperation.Type serviceOperationType = servicesOperationTypes.get(index);
            if (serviceOperationType != null) {
                triggeredServiceOperations.put(serviceName, serviceOperationType);
            }
        }
        context.setVariable(Variables.TRIGGERED_SERVICE_OPERATIONS, triggeredServiceOperations);
    }

    private List<CloudServiceInstanceExtended> generateCloudServicesExtended() {
        return serviceNames.stream()
                           .map(this::buildCloudServiceExtended)
                           .collect(Collectors.toList());
    }

    private ImmutableCloudServiceInstanceExtended buildCloudServiceExtended(String serviceName) {
        return ImmutableCloudServiceInstanceExtended.builder()
                                                    .name(serviceName)
                                                    .provider(TEST_PROVIDER)
                                                    .plan(TEST_PLAN)
                                                    .version(TEST_VERSION)
                                                    .metadata(ImmutableCloudMetadata.builder()
                                                                                    .guid(UUID.randomUUID())
                                                                                    .build())
                                                    .build();
    }

    private void prepareServicesData(List<CloudServiceInstanceExtended> services) {
        context.setVariable(Variables.SERVICES_DATA, services);
    }

    @Override
    protected CheckForOperationsInProgressStep createStep() {
        return new CheckForOperationsInProgressStep();
    }

    @Override
    protected void validateOperationExecutionResult(AsyncExecutionState result) {
        if (shouldVerifyStepLogger) {
            verify(stepLogger).warnWithoutProgressMessage(anyString(), any());
        }
        assertEquals(expectedExecutionState, result);
    }

    @Override
    protected List<AsyncExecution> getAsyncOperations(ProcessContext wrapper) {
        return step.getAsyncStepExecutions(wrapper);
    }
}
