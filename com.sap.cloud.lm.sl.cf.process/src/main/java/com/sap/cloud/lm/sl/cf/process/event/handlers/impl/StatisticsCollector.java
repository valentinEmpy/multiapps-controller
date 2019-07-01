package com.sap.cloud.lm.sl.cf.process.event.handlers.impl;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.util.RestUtil;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.process.adapters.FlowableEngineEventToDelegateExecutionAdapter;
import com.sap.cloud.lm.sl.cf.process.analytics.model.AnalyticsData;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.FlowableExecutionEventHandler;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnAbortProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.event.handlers.qualifiers.OnEndProcessQualifier;
import com.sap.cloud.lm.sl.cf.process.util.CollectedDataSender;
import com.sap.cloud.lm.sl.cf.web.api.model.State;
import com.sap.cloud.lm.sl.common.util.Runnable;

@Component
@OnAbortProcessQualifier
@OnEndProcessQualifier
public class StatisticsCollector implements FlowableEventHandler, FlowableExecutionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsCollector.class);
    private CollectedDataSender dataSender;
    private ApplicationConfiguration configuration;

    @Inject
    public StatisticsCollector(CollectedDataSender dataSender, ApplicationConfiguration configuration) {
        this.dataSender = dataSender;
        this.configuration = configuration;
    }

    @Override
    public void handle(DelegateExecution context) {
        if (configuration.shouldGatherUsageStatistics()) {
            executeSafely(() -> sendStatistics(context));
        }
    }

    @Override
    public void handle(FlowableEvent event) {
        if (event instanceof FlowableEngineEvent) {
            DelegateExecution context = new FlowableEngineEventToDelegateExecutionAdapter((FlowableEngineEvent) event);
            handle(context);
        }
    }

    private void sendStatistics(DelegateExecution context) {
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
