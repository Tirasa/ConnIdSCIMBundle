/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.v11;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.Response;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Client;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SCIMV11MockedTests {

    static class TestSCIMv11Client extends SCIMv11Client {

        private final WebClient webClient;

        TestSCIMv11Client(final SCIMConnectorConfiguration config) {
            super(config);

            webClient = mock(WebClient.class);

            when(webClient.path(any())).thenReturn(webClient);

            when(webClient.post(any())).thenAnswer(ic -> {
                Object payload = ic.getArgument(0);
                return Response.status(Response.Status.CREATED).entity(
                        "{\"id\":\"" + UUID.randomUUID().toString() + "\","
                        + payload.toString().substring(1)).
                        build();
            });

            when(webClient.get()).thenAnswer(ic -> {
                return Response.status(Response.Status.OK).entity(
                        "{\"schemas\":null,\"id\":\"d046ef30-a13a-490f-9a43-765a90abde33\","
                        + "\"externalId\":\"virtual-test-5\",\"active\":true,\"accountStatus\":\"Active\","
                        + "\"secondaryAccountStatus\":\"INVITED\",\"addresses\":null,"
                        + "\"displayName\":\"user testuser-test\","
                        + "\"emails\":[{\"type\":\"work\",\"value\":\"user@email.com\",\"primary\":true}],"
                        + "\"entitlements\":null,\"name\":{\"familyName\":\"testuser-test\",\"givenName\":\"user\"},"
                        + "\"phoneNumbers\":[{\"type\":\"work\",\"primary\":true}],\"userName\":\"virtual-test-5\","
                        + "\"urn:scim:schemas:core:1.0\":{\"ssoUniqueId\":\"\","
                        + "\"tokenAttributes\":\"{\\\"fullName\\\":\\\"user testuser-test\\\"}\","
                        + "\"memberOfOrganization\":null,\"marketplaceDomain\":\"{"
                        + "\\\"marketplaceDomain\\\":\\\"varis-ep-dev.odazcloud.com\\\","
                        + "\\\"marketplaceId\\\":\\\"5af39194\\\"}\"},"
                        + "\"meta\":{\"resourceType\":\"User\",\"created\":\"2023-06-27T14:30:39.510Z\","
                        + "\"lastModified\":\"2023-06-27T14:30:39.510Z\","
                        + "\"location\":\"https://localhost/Users/d046ef30-a13a-490f-9a43-765a90abde33\"}}").
                        build();
            });
            
            when(webClient.replaceHeader(anyString(), any())).thenReturn(webClient);
        }

        @Override
        protected WebClient getWebclient(final String path, final Map<String, String> params) {
            return webClient;
        }
    }

    private static SCIMConnectorConfiguration CONF;

    private static SCIMv11Connector CONN;

    private static TestSCIMv11Client CLIENT;

    @BeforeAll
    static void setUpConf() throws Exception {
        CONF = new SCIMConnectorConfiguration();
        CONF.setBaseAddress("https://localhost");
        CONF.setUsername("username");
        CONF.setPassword(new GuardedString("password".toCharArray()));
        CONF.setUseColonOnExtensionAttributes(false);
        CONF.setCustomAttributesJSON(
                "{\"id\":\"urn:scim:schemas:core:1.0:User\",\"name\":\"User\",\"description\":\"Core User\","
                + "\"schema\":\"urn:scim:schemas:core:1.0\",\"endpoint\":\"/Users\","
                + "\"attributes\":[{\"name\":\"partnerId\",\"type\":\"string\",\"multiValued\":false,"
                + "\"description\":\"\",\"schema\":\"urn:scim:schemas:core:1.0\",\"readOnly\":false,"
                + "\"required\":false,\"caseExact\":false},{\"name\":\"legalEntityId\",\"type\":\"string\","
                + "\"multiValued\":false,\"description\":\"\",\"schema\":\"urn:scim:schemas:core:1.0\","
                + "\"readOnly\":false,\"required\":false,\"caseExact\":false},{\"name\":\"appForActivation\","
                + "\"type\":\"string\",\"multiValued\":false,\"description\":\"\","
                + "\"schema\":\"urn:scim:schemas:core:1.0\",\"readOnly\":false,\"required\":false,"
                + "\"caseExact\":false},{\"name\":\"marketplaceId\",\"type\":\"string\",\"multiValued\":false,"
                + "\"description\":\"\",\"schema\":\"urn:scim:schemas:core:1.0\",\"readOnly\":false,\"required\":false,"
                + "\"caseExact\":false},{\"name\":\"marketplaceDomain\",\"type\":\"string\",\"multiValued\":false,"
                + "\"description\":\"\",\"schema\":\"urn:scim:schemas:core:1.0\",\"readOnly\":false,"
                + "\"required\":false,\"caseExact\":false},{\"name\":\"secondaryAccountStatus\",\"type\":\"string\","
                + "\"multiValued\":false,\"description\":\"\",\"schema\":\"urn:scim:schemas:core:1.0\","
                + "\"readOnly\":false,\"required\":false,\"caseExact\":false},{\"name\":\"accountStatus\","
                + "\"type\":\"string\",\"multiValued\":false,\"description\":\"\","
                + "\"schema\":\"urn:scim:schemas:core:1.0\",\"readOnly\":false,\"required\":false,"
                + "\"caseExact\":false}]}");
        CONF.validate();

        CONN = new SCIMv11Connector();
        CONN.init(CONF);

        CLIENT = new TestSCIMv11Client(CONF);

        Field client = AbstractSCIMConnector.class.getDeclaredField("client");
        client.setAccessible(true);
        client.set(CONN, CLIENT);
    }

    @Test
    void create() {
        Set<Attribute> createAttributes = new HashSet<>();
        createAttributes.add(AttributeBuilder.build("phoneNumbers.work.value"));
        createAttributes.add(AttributeBuilder.build("urn:scim:schemas:core:1.0.legalEntityId", "LENT-TDEG-ZC2Y-DE"));
        createAttributes.add(AttributeBuilder.build("urn:scim:schemas:core:1.0.marketplaceId", "5af39194"));
        createAttributes.add(AttributeBuilder.build("urn:scim:schemas:core:1.0.accountStatus", "created"));
        createAttributes.add(AttributeBuilder.build("name.familyName", "test"));
        createAttributes.add(AttributeBuilder.build("urn:scim:schemas:core:1.0.secondaryAccountStatus"));
        createAttributes.add(AttributeBuilder.buildEnabled(true));
        createAttributes.add(AttributeBuilder.build("urn:scim:schemas:core:1.0.partnerId", "PTNR-EVDY-ZC2Y-7S"));
        createAttributes.add(AttributeBuilder.build("name.givenName", "virtual"));
        createAttributes.add(AttributeBuilder.build("userName", "virtual-test-1"));
        createAttributes.add(AttributeBuilder.build("emails.work.value", "test@email.com"));
        createAttributes.add(new Name(null));

        assertDoesNotThrow(
                () -> CONN.create(ObjectClass.ACCOUNT, createAttributes, new OperationOptionsBuilder().build()));
    }

    @Test
    void read() {
        CONN.executeQuery(
                ObjectClass.ACCOUNT,
                new EqualsFilter(new Uid("d046ef30-a13a-490f-9a43-765a90abde33")),
                new SearchResultsHandler() {

            @Override
            public void handleResult(final SearchResult result) {
            }

            @Override
            public boolean handle(final ConnectorObject connectorObject) {
                Attribute attr = connectorObject.getAttributeByName("urn:scim:schemas:core:1.0.marketplaceDomain");
                assertNotNull(attr);
                assertEquals(
                        "{\"marketplaceDomain\":\"varis-ep-dev.odazcloud.com\",\"marketplaceId\":\"5af39194\"}",
                        attr.getValue().get(0));
                return true;
            }
        }, new OperationOptionsBuilder().setAttributesToGet(
                        "phoneNumbers.work.value", "urn:scim:schemas:core:1.0.externalId", "emails.work.value",
                        "__UID__", "userName", "__ENABLE__", "urn:scim:schemas:core:1.0.accountStatus",
                        "name.familyName",
                        "urn:scim:schemas:core:1.0.marketplaceDomain").build());
    }
}
