package com.sap.cloud.lm.sl.cf.core.dto.serialization;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaApplication;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaService;

@XmlAccessorType(XmlAccessType.FIELD)
public class DeployedMtaApplicationDto {

    private String moduleName;
    private String appName;
    @XmlElementWrapper(name = "services")
    @XmlElement(name = "service")
    private List<String> services;
    @XmlElementWrapper(name = "providedDependencies")
    @XmlElement(name = "providedDependency")
    private List<String> providedDependencyNames;

    protected DeployedMtaApplicationDto() {
        // Required by JAXB
    }

    public DeployedMtaApplicationDto(DeployedMtaApplication application) {
        this.moduleName = application.getModuleName();
        this.appName = application.getAppName();
        this.services = getServices(application);
        this.providedDependencyNames = application.getProvidedDependencyNames();
    }

    private List<String> getServices(DeployedMtaApplication application) {
        return application.getServices()
                          .stream()
                          .map(DeployedMtaService::getServiceName)
                          .collect(Collectors.toList());
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getAppName() {
        return appName;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getProvidedDependencyNames() {
        return providedDependencyNames;
    }

}
