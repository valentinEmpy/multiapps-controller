package com.sap.cloud.lm.sl.cf.core.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sap.cloud.lm.sl.cf.core.cf.HandlerFactory;
import com.sap.cloud.lm.sl.cf.core.helpers.v2.ConfigurationReferencesResolver;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.model.ResolvedConfigurationReference;
import com.sap.cloud.lm.sl.cf.core.model.SupportedParameters;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ConfigurationEntryService;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationURI;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.ApplicationNameValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.DomainValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.HostValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.ParameterValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.RestartOnEnvChangeValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.RoutesValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.ServiceNameValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.TasksValidator;
import com.sap.cloud.lm.sl.cf.core.validators.parameters.v3.VisibilityValidator;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;
import com.sap.cloud.lm.sl.mta.resolvers.NullPropertiesResolverBuilder;
import com.sap.cloud.lm.sl.mta.resolvers.ReferencesUnescaper;
import com.sap.cloud.lm.sl.mta.resolvers.ResolverBuilder;

public class MtaDescriptorPropertiesResolver {

    public static final String IDLE_DOMAIN_PLACEHOLDER = "${" + SupportedParameters.IDLE_DOMAIN + "}";
    public static final String IDLE_HOST_PLACEHOLDER = "${" + SupportedParameters.IDLE_HOST + "}";

    private final HandlerFactory handlerFactory;
    private final ConfigurationEntryService configurationEntryService;
    private final CloudTarget cloudTarget;
    private final String currentSpaceId;
    private List<ConfigurationSubscription> subscriptions;
    private final ApplicationConfiguration configuration;
    private final boolean useNamespaces;
    private final boolean useNamespacesForServices;
    private final boolean reserveTemporaryRoute;

    public MtaDescriptorPropertiesResolver(HandlerFactory handlerFactory, ConfigurationEntryService configurationEntryService,
        CloudTarget cloudTarget, String currentSpaceId, ApplicationConfiguration configuration, boolean useNamespaces,
        boolean useNamespacesForServices, boolean reserveTemporaryRoute) {
        this.handlerFactory = handlerFactory;
        this.configurationEntryService = configurationEntryService;
        this.cloudTarget = cloudTarget;
        this.currentSpaceId = currentSpaceId;
        this.configuration = configuration;
        this.useNamespaces = useNamespaces;
        this.useNamespacesForServices = useNamespacesForServices;
        this.reserveTemporaryRoute = reserveTemporaryRoute;
    }

    public List<ParameterValidator> getValidatorsList() {
        return Arrays.asList(new HostValidator(), new DomainValidator(), new RoutesValidator(), new TasksValidator(),
            new VisibilityValidator(), new RestartOnEnvChangeValidator());
    }

    public DeploymentDescriptor resolve(DeploymentDescriptor descriptor) {
        descriptor = correctEntityNames(descriptor);
        // Resolve placeholders in parameters:
        descriptor = handlerFactory
            .getDescriptorPlaceholderResolver(descriptor, new NullPropertiesResolverBuilder(), new ResolverBuilder(),
                SupportedParameters.SINGULAR_PLURAL_MAPPING)
            .resolve();

        if (reserveTemporaryRoute) {
            // temporary placeholders should be set at this point, since they are need for provides/requires placeholder resolution
            editRoutesSetTemporaryPlaceholders(descriptor);

            // Resolve again due to new temporary routes
            descriptor = handlerFactory
                .getDescriptorPlaceholderResolver(descriptor, new NullPropertiesResolverBuilder(), new ResolverBuilder(),
                    SupportedParameters.SINGULAR_PLURAL_MAPPING)
                .resolve();
        }

        List<ParameterValidator> validatorsList = getValidatorsList();
        descriptor = handlerFactory.getDescriptorParametersValidator(descriptor, validatorsList)
            .validate();

        // Resolve placeholders in properties:
        descriptor = handlerFactory
            .getDescriptorPlaceholderResolver(descriptor, new ResolverBuilder(), new NullPropertiesResolverBuilder(),
                SupportedParameters.SINGULAR_PLURAL_MAPPING)
            .resolve();

        DeploymentDescriptor descriptorWithUnresolvedReferences = DeploymentDescriptor.copyOf(descriptor);

        ConfigurationReferencesResolver resolver = handlerFactory.getConfigurationReferencesResolver(descriptor, configurationEntryService,
            cloudTarget, configuration);
        resolver.resolve(descriptor);

        subscriptions = createSubscriptions(descriptorWithUnresolvedReferences, resolver.getResolvedReferences());

        descriptor = handlerFactory
            .getDescriptorReferenceResolver(descriptor, new ResolverBuilder(), new ResolverBuilder(), new ResolverBuilder())
            .resolve();

        descriptor = handlerFactory.getDescriptorParametersValidator(descriptor, validatorsList, true)
            .validate();
        unescapeEscapedReferences(descriptor);

        return descriptor;
    }

    private void unescapeEscapedReferences(DeploymentDescriptor descriptor) {
        new ReferencesUnescaper().unescapeReferences(descriptor);
    }

    private DeploymentDescriptor correctEntityNames(DeploymentDescriptor descriptor) {
        List<ParameterValidator> correctors = Arrays.asList(new ApplicationNameValidator(descriptor.getId(), useNamespaces),
            new ServiceNameValidator(descriptor.getId(), useNamespaces, useNamespacesForServices));
        return handlerFactory.getDescriptorParametersValidator(descriptor, correctors)
            .validate();
    }

    private void editRoutesSetTemporaryPlaceholders(DeploymentDescriptor descriptor) {
        for (Module module : descriptor.getModules()) {
            Map<String, Object> moduleParameters = module.getParameters();
            if (moduleParameters.get(SupportedParameters.ROUTES) == null) {
                continue;
            }

            List<Map<String, Object>> routes = RoutesValidator.applyRoutesType(moduleParameters.get(SupportedParameters.ROUTES));

            for (Map<String, Object> route : routes) {
                Object routeValue = route.get(SupportedParameters.ROUTE);
                if (routeValue != null && routeValue instanceof String) {
                    route.put(SupportedParameters.ROUTE, replacePartsWithIdlePlaceholders((String) routeValue));
                }
            }
        }
    }

    private String replacePartsWithIdlePlaceholders(String uriString) {
        ApplicationURI uri = new ApplicationURI(uriString);
        uri.setDomain(IDLE_DOMAIN_PLACEHOLDER);
        uri.setHost(IDLE_HOST_PLACEHOLDER);
        return uri.toString();
    }

    private List<ConfigurationSubscription> createSubscriptions(DeploymentDescriptor descriptorWithUnresolvedReferences,
        Map<String, ResolvedConfigurationReference> resolvedResources) {
        return handlerFactory.getConfigurationSubscriptionFactory()
            .create(descriptorWithUnresolvedReferences, resolvedResources, currentSpaceId);
    }

    public List<ConfigurationSubscription> getSubscriptions() {
        return subscriptions;
    }

}
