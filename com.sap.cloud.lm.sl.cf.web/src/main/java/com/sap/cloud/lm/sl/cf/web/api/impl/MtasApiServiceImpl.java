package com.sap.cloud.lm.sl.cf.web.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.springframework.http.ResponseEntity;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.cf.detect.DeployedComponentsDetector;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.MtaMetadata;
import com.sap.cloud.lm.sl.cf.core.util.UserInfo;
import com.sap.cloud.lm.sl.cf.web.api.MtasApiService;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableModule;
import com.sap.cloud.lm.sl.cf.web.api.model.ImmutableMta;
import com.sap.cloud.lm.sl.cf.web.api.model.Metadata;
import com.sap.cloud.lm.sl.cf.web.api.model.Module;
import com.sap.cloud.lm.sl.cf.web.api.model.Mta;
import com.sap.cloud.lm.sl.cf.web.message.Messages;
import com.sap.cloud.lm.sl.cf.web.util.SecurityContextUtil;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Named
public class MtasApiServiceImpl implements MtasApiService {

    @Inject
    private CloudControllerClientProvider clientProvider;

    @Inject
    private DeployedComponentsDetector deployedComponentsDetector;

    @Override
    public ResponseEntity<List<Mta>> getMtas(String spaceGuid) {
        List<DeployedMta> deployedMtas = deployedComponentsDetector.getAllDeployedMtas(getCloudFoundryClient(spaceGuid));
        List<Mta> mtas = getMtas(deployedMtas);
        return ResponseEntity.ok()
                             .body(mtas);
    }

    @Override
    public ResponseEntity<Mta> getMta(String spaceGuid, String mtaId) {
        Optional<DeployedMta> optionalDeployedMta = deployedComponentsDetector.getDeployedMta(mtaId, getCloudFoundryClient(spaceGuid));
        DeployedMta deployedMta = optionalDeployedMta.orElseThrow(() -> new NotFoundException(Messages.MTA_NOT_FOUND, mtaId));
        return ResponseEntity.ok()
                             .body(getMta(deployedMta));
    }

    private CloudControllerClient getCloudFoundryClient(String spaceGuid) {
        UserInfo userInfo = SecurityContextUtil.getUserInfo();
        return clientProvider.getControllerClient(userInfo.getName(), spaceGuid);
    }

    private List<Mta> getMtas(List<DeployedMta> deployedMtas) {
        List<Mta> mtas = new ArrayList<>();
        for (DeployedMta mta : deployedMtas) {
            mtas.add(getMta(mta));
        }

        return mtas;
    }

    private Mta getMta(DeployedMta mta) {
        return ImmutableMta.builder()
                           .metadata(getMetadata(mta.getMetadata()))
                           .modules(getModules(mta.getModules()))
                           .services(mta.getResources()
                                        .stream()
                                        .map(s -> s.getServiceName())
                                        .collect(Collectors.toSet()))
                           .build();
    }

    private List<Module> getModules(List<DeployedMtaModule> modules) {
        return modules.stream()
                      .map(this::getModule)
                      .collect(Collectors.toList());
    }

    private Module getModule(DeployedMtaModule module) {
        return ImmutableModule.builder()
                              .appName(module.getAppName())
                              .moduleName(module.getModuleName())
                              .providedDendencyNames(module.getProvidedDependencyNames())
                              .uris(module.getUris())
                              .services(module.getResources()
                                              .stream()
                                              .map(s -> s.getServiceName())
                                              .collect(Collectors.toList()))
                              .build();
    }

    private Metadata getMetadata(MtaMetadata metadata) {
        return ImmutableMetadata.builder()
                                .id(metadata.getId())
                                .version(metadata.getVersion()
                                                 .toString())
                                .build();
    }

}
