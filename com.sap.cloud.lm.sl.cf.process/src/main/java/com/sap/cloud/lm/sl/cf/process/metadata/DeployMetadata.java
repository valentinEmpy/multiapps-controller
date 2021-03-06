package com.sap.cloud.lm.sl.cf.process.metadata;

import java.util.HashSet;
import java.util.Set;

import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableOperationMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableParameterMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.OperationMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.ParameterMetadata.ParameterType;

public class DeployMetadata {

    private static final Set<ImmutableParameterMetadata> PARAMS = new HashSet<>();

    static {
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.APP_ARCHIVE_ID.getName())
                                             .type(ParameterType.STRING)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.EXT_DESCRIPTOR_FILE_ID.getName())
                                             .type(ParameterType.STRING)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.NO_START.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.NO_START.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.START_TIMEOUT.getName())
                                             .type(ParameterType.INTEGER)
                                             .defaultValue(Variables.START_TIMEOUT.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.USE_NAMESPACES.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.USE_NAMESPACES.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.USE_NAMESPACES_FOR_SERVICES.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.USE_NAMESPACES_FOR_SERVICES.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.VERSION_RULE.getName())
                                             .type(ParameterType.STRING)
                                             .defaultValue(Variables.VERSION_RULE.getDefaultValue()
                                                                                 .toString())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.DELETE_SERVICES.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.DELETE_SERVICES.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.DELETE_SERVICE_KEYS.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.DELETE_SERVICE_KEYS.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.DELETE_SERVICE_BROKERS.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.DELETE_SERVICE_BROKERS.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.FAIL_ON_CRASHED.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.FAIL_ON_CRASHED.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.MTA_ID.getName())
                                             .type(ParameterType.STRING)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.KEEP_FILES.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.KEEP_FILES.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.NO_RESTART_SUBSCRIBED_APPS.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.NO_RESTART_SUBSCRIBED_APPS.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.NO_FAIL_ON_MISSING_PERMISSIONS.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.NO_FAIL_ON_MISSING_PERMISSIONS.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.GIT_URI.getName())
                                             .type(ParameterType.STRING)
                                             .defaultValue(Variables.GIT_URI.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.GIT_REF.getName())
                                             .type(ParameterType.STRING)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.GIT_REPO_PATH.getName())
                                             .type(ParameterType.STRING)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.GIT_SKIP_SSL.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.GIT_SKIP_SSL.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.ABORT_ON_ERROR.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.ABORT_ON_ERROR.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.MODULES_FOR_DEPLOYMENT.getName())
                                             .type(ParameterType.STRING)
                                             .defaultValue(null)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.RESOURCES_FOR_DEPLOYMENT.getName())
                                             .type(ParameterType.STRING)
                                             .defaultValue(null)
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.VERIFY_ARCHIVE_SIGNATURE.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.VERIFY_ARCHIVE_SIGNATURE.getDefaultValue())
                                             .build());
        PARAMS.add(ImmutableParameterMetadata.builder()
                                             .id(Variables.ENABLE_ENV_DETECTION.getName())
                                             .type(ParameterType.BOOLEAN)
                                             .defaultValue(Variables.ENABLE_ENV_DETECTION.getDefaultValue())
                                             .required(false)
                                             .build());
    }

    private DeployMetadata() {
    }

    public static OperationMetadata getMetadata() {
        return ImmutableOperationMetadata.builder()
                                         .parameters(PARAMS)
                                         .diagramId(Constants.DEPLOY_SERVICE_ID)
                                         .addVersions(Constants.SERVICE_VERSION_1_1, Constants.SERVICE_VERSION_1_2)
                                         .build();
    }

}
