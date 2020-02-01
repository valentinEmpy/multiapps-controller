package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudMetadata;
import org.cloudfoundry.client.lib.domain.ImmutableCloudService;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceExtended;
import com.sap.cloud.lm.sl.cf.core.model.ServiceOperation;
import com.sap.cloud.lm.sl.cf.core.util.MethodExecution;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Named("updateServiceMetadataStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateServiceMetadataStep extends ServiceStep {

    @Override
    protected MethodExecution<String> executeOperation(DelegateExecution execution, CloudControllerClient controllerClient,
                                                       CloudServiceExtended service) {
        return updateServiceMetadata(execution, controllerClient, service);
    }

    private MethodExecution<String> updateServiceMetadata(DelegateExecution context, CloudControllerClient client,
                                                          CloudServiceExtended service) {
        getStepLogger().debug(Messages.UPDATING_SERVICE_METADATA, service.getName(), service.getResourceName());
        updateServiceMetadata(service, client);
        getStepLogger().debug(Messages.SERVICE_METADATA_UPDATED, service.getName());
        return new MethodExecution<>(null, MethodExecution.ExecutionState.FINISHED);
    }

    private void updateServiceMetadata(CloudServiceExtended serviceToProcess, CloudControllerClient client) {
        ImmutableCloudService serviceWithMetadata = ImmutableCloudService.copyOf(serviceToProcess);
        CloudMetadata serviceMeta = client.getService(serviceWithMetadata.getName())
                                          .getMetadata();
        serviceWithMetadata = serviceWithMetadata.withMetadata(serviceMeta);
        client.updateServiceMetadata(serviceWithMetadata.getMetadata()
                                                        .getGuid(),
                                     serviceWithMetadata.getV3Metadata());
        getStepLogger().debug("updated service metadata name: " + serviceWithMetadata + " metadata: "
            + JsonUtil.toJson(serviceWithMetadata.getV3Metadata(), true));
    }

    @Override
    protected List<AsyncExecution> getAsyncStepExecutions(ExecutionWrapper execution) {
        return Collections.singletonList(new PollServiceCreateOrUpdateOperationsExecution(getServiceOperationGetter(),
                                                                                          getServiceProgressReporter()));
    }

    @Override
    protected ServiceOperation.Type getOperationType() {
        return ServiceOperation.Type.UPDATE;
    }

}
