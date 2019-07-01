package com.sap.cloud.lm.sl.cf.process.handlers.impl;

import org.cloudfoundry.client.lib.util.RestUtil;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.cf.process.analytics.model.AnalyticsData;
import com.sap.cloud.lm.sl.cf.process.handlers.FlowableEngineEventHandler;
import com.sap.cloud.lm.sl.cf.process.util.CollectedDataSender;
import com.sap.cloud.lm.sl.cf.web.api.model.State;
import com.sap.cloud.lm.sl.common.util.Runnable;

public class AnalyticsCollector extends FlowableEngineEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsCollector.class);
    private CollectedDataSender dataSender;
    private ApplicationConfiguration configuration;

    public AnalyticsCollector(CollectedDataSender dataSender, ApplicationConfiguration configuration) {
        this.dataSender = dataSender;
        this.configuration = configuration;
    }

    @Override
    protected void handleInternal(FlowableEvent event) {
        if (configuration.shouldGatherUsageStatistics()) {
            executeSafely(() -> sendStatistics(event));
        }
    }

    private void sendStatistics(FlowableEvent event) {
        DelegateExecution context = new FlowableEngineEventToDelegateExecutionAdapter((FlowableEngineEvent) event);
        RestTemplate restTemplate = new RestUtil().createRestTemplate(null, false);
        AnalyticsData collectedData = dataSender.collectAnalyticsData(context, State.ABORTED);
        dataSender.sendCollectedData(restTemplate, dataSender.convertCollectedAnalyticsDataToXml(context, collectedData));
    }

    private void executeSafely(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) { // NOSONAR
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
