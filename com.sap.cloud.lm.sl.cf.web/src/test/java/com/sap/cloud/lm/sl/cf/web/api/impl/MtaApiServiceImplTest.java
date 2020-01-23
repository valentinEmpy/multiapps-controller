package com.sap.cloud.lm.sl.cf.web.api.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.cf.detect.DeployedMtaDetector;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMta;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaModule;
import com.sap.cloud.lm.sl.cf.core.model.DeployedMtaResource;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.MtaMetadata;
import com.sap.cloud.lm.sl.cf.web.api.model.Metadata;
import com.sap.cloud.lm.sl.cf.web.api.model.Module;
import com.sap.cloud.lm.sl.cf.web.api.model.Mta;
import com.sap.cloud.lm.sl.cf.web.security.AuthorizationChecker;
import com.sap.cloud.lm.sl.common.NotFoundException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.TestUtil;
import com.sap.cloud.lm.sl.mta.model.Version;

public class MtaApiServiceImplTest {

    @Mock
    private CloudControllerClientProvider clientProvider;

    @Mock
    private AuthorizationChecker authorizationChecker;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CloudControllerClient client;

    @Mock
    private DeployedMtaDetector deployedMtaDetector;

    @InjectMocks
    private MtasApiServiceImpl testedClass;

    List<CloudApplication> apps;
    List<Mta> mtas;

    private static final String USER_NAME = "someUser";
    private static final String SPACE_GUID = "896e6be9-8217-4a1c-b938-09b30966157a";

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        apps = parseApps();
        mtas = parseMtas();
        mockClient(USER_NAME);
    }

    private List<CloudApplication> parseApps() {
        String appsJson = TestUtil.getResourceAsString("apps-01.json", getClass());
        return JsonUtil.fromJson(appsJson, new TypeReference<List<CloudApplication>>() {
        });
    }

    private List<Mta> parseMtas() {
        String appsJson = TestUtil.getResourceAsString("mtas-01.json", getClass());
        return JsonUtil.fromJson(appsJson, new TypeReference<List<Mta>>() {
        });
    }

    @Test
    public void testGetMtas() {
        ResponseEntity<List<Mta>> response = testedClass.getMtas(SPACE_GUID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Mta> responseMtas = response.getBody();
        mtas.equals(responseMtas);
    }

    @Test
    public void testGetMta() {
        Mta mtaToGet = mtas.get(1);
        ResponseEntity<Mta> response = testedClass.getMta(SPACE_GUID, mtaToGet.getMetadata()
                                                                              .getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Mta responseMtas = response.getBody();
        mtaToGet.equals(responseMtas);
    }

    @Test
    public void testGetMtaNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> testedClass.getMta(SPACE_GUID, "not_a_real_mta"));
    }

    private void mockClient(String user) {
        com.sap.cloud.lm.sl.cf.core.util.UserInfo userInfo = new com.sap.cloud.lm.sl.cf.core.util.UserInfo(null, user, null);
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal())
               .thenReturn(userInfo);
        org.springframework.security.core.context.SecurityContext securityContextMock = Mockito.mock(org.springframework.security.core.context.SecurityContext.class);
        SecurityContextHolder.setContext(securityContextMock);
        Mockito.when(securityContextMock.getAuthentication())
               .thenReturn(auth);
        Mockito.when(clientProvider.getControllerClient(Mockito.anyString(), Mockito.anyString()))
               .thenReturn(client);
        Mockito.when(client.getApplications())
               .thenReturn(apps);
        Mockito.when(deployedMtaDetector.getAllDeployedMtas(Mockito.any()))
               .thenReturn(getDeployedMtas(mtas));
        Mockito.when(deployedMtaDetector.getDeployedMta(mtas.get(1)
                                                                   .getMetadata()
                                                                   .getId(),
                                                               client))
               .thenReturn(Optional.of(getDeployedMta(mtas.get(1))));
    }

    private List<DeployedMta> getDeployedMtas(List<Mta> mtas) {
        return mtas.stream()
                   .map(this::getDeployedMta)
                   .collect(Collectors.toList());
    }

    private DeployedMta getDeployedMta(Mta mta) {
        return DeployedMta.builder()
                          .withMetadata(getMtaMetadata(mta.getMetadata()))
                          .withModules(getDeployedMtaModules(mta.getModules()))
                          .withResources(mta.getServices()
                                            .stream()
                                            .map(this::getDeployedMtaResource)
                                            .collect(Collectors.toList()))
                          .build();
    }

    private List<DeployedMtaModule> getDeployedMtaModules(List<Module> modules) {
        return modules.stream()
                      .map(this::getDeployedMtaModule)
                      .collect(Collectors.toList());
    }

    private DeployedMtaModule getDeployedMtaModule(Module module) {
        DeployedMtaModule deployedMtaModule = new DeployedMtaModule();
        deployedMtaModule.setAppName(module.getAppName());
        deployedMtaModule.setModuleName(module.getModuleName());
        deployedMtaModule.setProvidedDependencyNames(module.getProvidedDendencyNames());
        deployedMtaModule.setUris(module.getUris());
        deployedMtaModule.setResources(module.getServices()
                                             .stream()
                                             .map(this::getDeployedMtaResource)
                                             .collect(Collectors.toList()));
        return deployedMtaModule;
    }

    private DeployedMtaResource getDeployedMtaResource(String service) {
        DeployedMtaResource deployedMtaResource = new DeployedMtaResource();
        deployedMtaResource.setServiceName(service);
        return deployedMtaResource;
    }

    private MtaMetadata getMtaMetadata(Metadata metadata) {
        MtaMetadata mtaMetadata = new MtaMetadata();
        mtaMetadata.setId(metadata.getId());
        mtaMetadata.setVersion(Version.parseVersion(metadata.getVersion()));
        return mtaMetadata;

    }

}
