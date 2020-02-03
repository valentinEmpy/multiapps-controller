package com.sap.cloud.lm.sl.cf.process.helpers;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.flowable.variable.api.history.HistoricVariableInstance;

import com.sap.cloud.lm.sl.cf.core.message.Messages;
import com.sap.cloud.lm.sl.cf.core.model.ApplicationColor;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.Phase;
import com.sap.cloud.lm.sl.cf.core.persistence.OrderDirection;
import com.sap.cloud.lm.sl.cf.core.persistence.service.OperationService;
import com.sap.cloud.lm.sl.cf.core.util.CloudModelBuilderUtil;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.web.api.model.Operation;
import com.sap.cloud.lm.sl.cf.web.api.model.ProcessType;
import com.sap.cloud.lm.sl.common.ConflictException;

@Named("applicationColorDetector")
public class ApplicationColorDetector {

    @Inject
    private OperationService operationService;

    @Inject
    private FlowableFacade flowableFacade;

    public ApplicationColor detectLiveApplicationColor(DeployedMta deployedMta, String correlationId) {
        if (deployedMta == null) {
            return null;
        }
        ApplicationColor olderApplicationColor = getOlderApplicationColor(deployedMta);
        Operation currentOperation = operationService.createQuery()
                                                     .processId(correlationId)
                                                     .singleResult();

        List<Operation> operations = operationService.createQuery()
                                                     .mtaId(currentOperation.getMtaId())
                                                     .processType(ProcessType.BLUE_GREEN_DEPLOY)
                                                     .spaceId(currentOperation.getSpaceId())
                                                     .inFinalState()
                                                     .orderByEndTime(OrderDirection.DESCENDING)
                                                     .limitOnSelect(1)
                                                     .list();
        if (CollectionUtils.isEmpty(operations)) {
            return olderApplicationColor;
        }

        if (operations.get(0)
                      .getState() != Operation.State.ABORTED) {
            return olderApplicationColor;
        }
        String xs2BlueGreenDeployHistoricProcessInstanceId = flowableFacade.findHistoricProcessInstanceIdByProcessDefinitionKey(operations.get(0)
                                                                                                                                          .getProcessId(),
                                                                                                                                Constants.BLUE_GREEN_DEPLOY_SERVICE_ID);

        ApplicationColor latestDeployedColor = getColorFromHistoricProcess(xs2BlueGreenDeployHistoricProcessInstanceId);
        Phase phase = getPhaseFromHistoricProcess(xs2BlueGreenDeployHistoricProcessInstanceId);

        if (latestDeployedColor == null) {
            return olderApplicationColor;
        }
        return phase == Phase.UNDEPLOY ? latestDeployedColor : latestDeployedColor.getAlternativeColor();
    }

    public ApplicationColor detectSingularDeployedApplicationColor(DeployedMta deployedMta) {
        if (deployedMta == null) {
            return null;
        }
        ApplicationColor deployedApplicationColor = null;
        for (DeployedMtaApplication deployedMtaApplication : deployedMta.getApplications()) {
            ApplicationColor applicationColor = CloudModelBuilderUtil.getApplicationColor(deployedMtaApplication);
            if (deployedApplicationColor == null) {
                deployedApplicationColor = (applicationColor);
            }
            if (deployedApplicationColor != applicationColor) {
                throw new ConflictException(Messages.CONFLICTING_APP_COLORS,
                                            deployedMta.getMetadata()
                                                       .getId());
            }
        }
        return deployedApplicationColor;
    }

    private ApplicationColor getOlderApplicationColor(DeployedMta deployedMta) {
        return deployedMta.getApplications()
                          .stream()
                          .filter(application -> application.getMetadata() != null)
                          .min(Comparator.comparing(application -> application.getMetadata()
                                                                              .getCreatedAt()))
                          .map(CloudModelBuilderUtil::getApplicationColor)
                          .orElse(null);
    }

    private Phase getPhaseFromHistoricProcess(String processInstanceId) {
        HistoricVariableInstance phaseVariableInstance = flowableFacade.getHistoricVariableInstance(processInstanceId, Constants.VAR_PHASE);
        if (phaseVariableInstance == null) {
            return null;
        }

        return Phase.valueOf((String) phaseVariableInstance.getValue());
    }

    private ApplicationColor getColorFromHistoricProcess(String processInstanceId) {
        HistoricVariableInstance colorVariableInstance = flowableFacade.getHistoricVariableInstance(processInstanceId,
                                                                                                    Constants.VAR_IDLE_MTA_COLOR);

        if (colorVariableInstance == null) {
            colorVariableInstance = flowableFacade.getHistoricVariableInstance(processInstanceId, Constants.VAR_MTA_COLOR);
            if (colorVariableInstance == null) {
                return null;
            }
        }

        return ApplicationColor.valueOf((String) colorVariableInstance.getValue());
    }

}
