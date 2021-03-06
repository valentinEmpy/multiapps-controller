package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudTask;
import org.cloudfoundry.client.lib.domain.ImmutableCloudTask;
import org.junit.Test;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;

public class PrepareToExecuteTasksStepTest extends SyncFlowableStepTest<PrepareToExecuteTasksStep> {

    @Override
    protected PrepareToExecuteTasksStep createStep() {
        return new PrepareToExecuteTasksStep();
    }

    @Test
    public void testIterationOverTasksIsInitialized() {
        // Given:
        context.setVariable(Variables.TASKS_TO_EXECUTE, createDummyApplicationWithTasks(3).getTasks());

        // When:
        step.execute(execution);

        // Then:
        assertStepFinishedSuccessfully();
        assertEquals((Integer) 3, context.getVariable(Variables.TASKS_COUNT));
        assertEquals((Integer) 0, context.getVariable(Variables.TASKS_INDEX));
        assertEquals(Variables.TASKS_INDEX.getName(), context.getVariable(Variables.INDEX_VARIABLE_NAME));
        assertEquals(3, context.getVariable(Variables.TASKS_TO_EXECUTE)
                               .size());
    }

    @Test
    public void testExecuteWhenTasksAreSupported() {
        // Given:
        context.setVariable(Variables.TASKS_TO_EXECUTE, createDummyApplicationWithTasks(0).getTasks());

        // When:
        step.execute(execution);

        assertStepFinishedSuccessfully();
    }

    private CloudApplicationExtended createDummyApplicationWithTasks(int numberOfTasks) {
        return ImmutableCloudApplicationExtended.builder()
                                                .name("dummy")
                                                .tasks(createDummyTasks(numberOfTasks))
                                                .build();
    }

    private List<CloudTask> createDummyTasks(int count) {
        List<CloudTask> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(createDummyTask());
        }
        return result;
    }

    private CloudTask createDummyTask() {
        return ImmutableCloudTask.builder()
                                 .name("dummy")
                                 .build();
    }

}
