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
package net.tirasa.connid.bundles.scim.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.PagedResults;
import net.tirasa.connid.bundles.scim.common.dto.ResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGenericComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUserAddress;
import net.tirasa.connid.bundles.scim.common.service.NoSuchEntityException;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMUserName;
import net.tirasa.connid.bundles.scim.v2.dto.Mutability;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Entitlement;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Group;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.dto.Uniqueness;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.SortKey;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.test.common.TestHelpers;
import org.identityconnectors.test.common.ToListResultsHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class SCIMv2ConnectorTests {

    private static final Log LOG = Log.getLog(SCIMv2ConnectorTests.class);

    private static final Properties PROPS = new Properties();

    private static SCIMConnectorConfiguration CONF;

    private static SCIMv2Connector CONN;

    private static ConnectorFacade FACADE;

    private static final List<String> CUSTOM_OTHER_SCHEMAS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_KEYS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_VALUES = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_UPDATE_VALUES = new ArrayList<>();

    @Container
    private static final GenericContainer<?> SCIMPLE_SERVER =
            new GenericContainer<>("tirasa/scimple-server:1.0.0").withExposedPorts(8080)
                    .waitingFor(Wait.forLogMessage(".*Started ScimpleSpringBootApplication in.*\\n", 1));

    @BeforeAll
    public static void setUpConf() throws IOException {
        PROPS.load(SCIMv2ConnectorTests.class.getResourceAsStream("/net/tirasa/connid/bundles/scim/authv2.properties"));

        Map<String, String> configurationParameters = new HashMap<>();
        for (final String name : PROPS.stringPropertyNames()) {
            configurationParameters.put(name, PROPS.getProperty(name));
        }
        CONF = SCIMv2ConnectorTestsUtils.buildConfiguration(
                configurationParameters, SCIMPLE_SERVER.getFirstMappedPort());

        Boolean isValid = SCIMv2ConnectorTestsUtils.isConfigurationValid(CONF);
        if (isValid) {
            CONN = new SCIMv2Connector();
            CONN.init(CONF);
            try {
                CONN.test();
            } catch (Exception e) {
                LOG.error(e, "While testing connector");
            }
            CONN.schema();
        }

        // custom schemas
        if (PROPS.containsKey("auth.otherSchemas") && PROPS.getProperty("auth.otherSchemas") != null) {
            CUSTOM_OTHER_SCHEMAS.addAll(Arrays.asList(PROPS.getProperty("auth.otherSchemas").split("\\s*,\\s*")));
        }
        CUSTOM_OTHER_SCHEMAS.add("urn:ietf:params:scim:schemas:core:2.0:User");

        // custom attributes
        if (PROPS.containsKey("auth.customAttributesValues")
                && PROPS.getProperty("auth.customAttributesValues") != null) {
            CUSTOM_ATTRIBUTES_VALUES.addAll(
                    Arrays.asList(PROPS.getProperty("auth.customAttributesValues").split("\\s*,\\s*")));
        }
        if (PROPS.containsKey("auth.customAttributesKeys") && PROPS.getProperty("auth.customAttributesKeys") != null) {
            CUSTOM_ATTRIBUTES_KEYS.addAll(
                    Arrays.asList(PROPS.getProperty("auth.customAttributesKeys").split("\\s*,\\s*")));
        }
        if (PROPS.containsKey("auth.customAttributesUpdateValues")
                && PROPS.getProperty("auth.customAttributesUpdateValues") != null) {
            CUSTOM_ATTRIBUTES_UPDATE_VALUES.addAll(
                    Arrays.asList(PROPS.getProperty("auth.customAttributesUpdateValues").split("\\s*,\\s*")));
        }

        FACADE = newFacade();

        assertNotNull(CONF);
        assertNotNull(isValid);
        assertNotNull(CONF.getBaseAddress());
        assertNotNull(CONF.getPassword());
        assertNotNull(CONF.getUsername());
        assertNotNull(CONF.getAccept());
        assertNotNull(CONF.getContentType());
        assertNotNull(CONF.getUpdateUserMethod());
    }

    private static ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(SCIMv2Connector.class, CONF);
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    private static SCIMv2Client newClient() {
        return CONN.getClient();
    }

    private static Uid createUser(final UUID uid, final String... groups) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString(SCIMv2ConnectorTestsUtils.VALUE_PASSWORD.toCharArray()));
        String name = SCIMv2ConnectorTestsUtils.VALUE_USERNAME + uid.toString().substring(0, 10) + "@email.com";

        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME, name));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_NICK_NAME + uid.toString().substring(0, 10)));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE, name));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_VALUE,
                SCIMv2ConnectorTestsUtils.VALUE_PHONE_NUMBER));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_PRIMARY, false));
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE, true));
        userAttrs.add(password);

        if (PROPS.containsKey("auth.defaultEntitlement") && StringUtil.isNotBlank(
                PROPS.getProperty("auth.defaultEntitlement"))) {
            userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE,
                    PROPS.getProperty("auth.defaultEntitlement")));
        }

        // custom attributes
        addCustomAttributes(userAttrs);

        // enterprise v2 user info
        userAttrs.add(
                AttributeBuilder.build("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.employeeNumber",
                        "12345"));
        userAttrs.add(AttributeBuilder.build("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.manager.value",
                "bulkId:qwerty"));

        //        urn:scim:schemas:extension:enterprise:1.0:employeeNumber
        //        urn:scim:schemas:extension:enterprise:1.0:manager.managerId
        //        urn:scim:schemas:extension:enterprise:1.0:manager.displayName
        // custom schemas
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOM_OTHER_SCHEMAS));

        // add groups
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_GROUPS, (Object[]) groups));

        Uid created = FACADE.create(ObjectClass.ACCOUNT, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(created);
        assertFalse(created.getUidValue().isEmpty());
        LOG.info("Created User uid: {0}", created);

        return created;
    }

    private static Uid updateUser(final Uid created, final String name, final String... groups) {
        Set<Attribute> userAttrs = updateUserAttributes(created, name);

        // change groups
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_GROUPS, (Object[]) groups));

        Uid updated = FACADE.update(ObjectClass.ACCOUNT, created, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated User uid: {0}", updated);

        return updated;
    }

    private static Uid updateUser(final Uid created, final String name, final List<String> groupsToAdd,
            final List<String> groupsToRemove) {
        Set<Attribute> userAttrs = updateUserAttributes(created, name);

        // change groups
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_GROUPS_TO_ADD, groupsToAdd));
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_GROUPS_TO_REMOVE, groupsToRemove));

        Uid updated = FACADE.update(ObjectClass.ACCOUNT, created, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated User uid: {0}", updated);

        return updated;
    }

    private static Set<Attribute> updateUserAttributes(final Uid created, final String name) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString((SCIMv2ConnectorTestsUtils.VALUE_PASSWORD + "01").toCharArray()));
        // UPDATE USER VALUE_PASSWORD
        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME, name));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_NICK_NAME + created.getUidValue().substring(0, 10)));
        userAttrs.add(
                AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE, "updated" + name));
        // no phone number -> delete
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_HOME_VALUE, "123456789"));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_HOME_PRIMARY, true));
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE, true));
        userAttrs.add(password);

        if (PROPS.containsKey("auth.defaultEntitlement") && StringUtil.isNotBlank(
                PROPS.getProperty("auth.defaultEntitlement"))) {
            userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE,
                    PROPS.getProperty("auth.defaultEntitlement")));
        }

        // custom attributes
        addCustomAttributes(userAttrs);

        // custom schemas
        CUSTOM_OTHER_SCHEMAS.add(SCIMv2EnterpriseUser.SCHEMA_URI);
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOM_OTHER_SCHEMAS));

        userAttrs.add(
                AttributeBuilder.build("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.employeeNumber",
                        "56789"));
        userAttrs.add(AttributeBuilder.build("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.manager.value",
                "bulkId:asdsdfas"));
        return userAttrs;
    }

    private static Uid createGroup(final UUID uid, final String prefix) {
        Set<Attribute> groupAttrs = new HashSet<>();
        String displayName = prefix + "_group_" + uid.toString().substring(0, 10);
        groupAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME, displayName));
        groupAttrs.add(new Name(displayName));
        Uid created = FACADE.create(ObjectClass.GROUP, groupAttrs, new OperationOptionsBuilder().build());
        assertNotNull(created);
        assertFalse(created.getUidValue().isEmpty());
        LOG.info("Created Group uid: {0}", created);

        return created;
    }

    private static Uid updateGroup(final Uid groupToUpdate, final String newDisplayName) {
        Set<Attribute> groupAttrs = new HashSet<>();
        groupAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME, newDisplayName));
        groupAttrs.add(new Name(newDisplayName));

        Uid updated =
                FACADE.update(ObjectClass.GROUP, groupToUpdate, groupAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated Group uid: {0}", updated);

        return updated;
    }

    private static void deleteUser(final Uid userToDelete) {
        FACADE.delete(ObjectClass.ACCOUNT, userToDelete, new OperationOptionsBuilder().build());
    }

    private static void deleteGroup(final Uid groupToDelete) {
        FACADE.delete(ObjectClass.GROUP, groupToDelete, new OperationOptionsBuilder().build());
    }

    private static SCIMv2User readUser(final String id, final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        SCIMv2User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(user.getName().getFamilyName(), SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME);
        assertFalse(user.getEmails().isEmpty());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes(user.getClass(), CONF);
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement") && StringUtil.isNotBlank(
                PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv2ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS));
        }

        List<ConnectorObject> found = new ArrayList<>();
        if (testCustomAttributes()) {
            FACADE.search(ObjectClass.ACCOUNT, new EqualsFilter(new Name(user.getUserName())), found::add,
                    new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                            "displayName", "active",
                            "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
            assertEquals(1, found.size());
            assertNotNull(found.get(0));
            assertNotNull(found.get(0).getName());
            for (String key : CUSTOM_ATTRIBUTES_KEYS) {
                assertNotNull(found.get(0).getAttributeByName(key));
                assertNotNull(found.get(0).getAttributeByName(key).getValue());
                assertFalse(found.get(0).getAttributeByName(key).getValue().isEmpty());
            }
            LOG.info("Found User using Connector search: {0}", found.get(0));
        }
        found.clear();
        FACADE.search(ObjectClass.ACCOUNT, new EqualsFilter(new Name(user.getUserName())), found::add,
                new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                        "displayName", "active", SCIMv2EnterpriseUser.SCHEMA_URI + ".employeeNumber",
                        SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.value",
                        "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(found.get(0).getAttributes(),
                SCIMv2EnterpriseUser.SCHEMA_URI + ".employeeNumber"));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(found.get(0).getAttributes(),
                SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.value"));

        return user;
    }

    private static SCIMv2Group readGroup(final String id, final SCIMv2Client client) throws IllegalArgumentException {
        SCIMv2Group group = client.getGroup(id);
        assertNotNull(group);
        assertNotNull(group.getId());
        assertNotNull(group.getDisplayName());
        LOG.info("Found Group: {0}", group);

        return group;
    }

    private static SCIMv2User createUserServiceTest(final UUID uid, final boolean active,
            final List<ResourceReference> groups, final SCIMv2Client client) {
        SCIMv2User user = new SCIMv2User();
        String name = SCIMv2ConnectorTestsUtils.VALUE_USERNAME + uid.toString().substring(0, 10) + "@email.com";
        user.getSchemas().add("urn:mem:params:scim:schemas:extension:LuckyNumberExtension");
        user.setUserName(name);
        user.setPassword(SCIMv2ConnectorTestsUtils.VALUE_PASSWORD);
        user.getSchemas().addAll(CUSTOM_OTHER_SCHEMAS);
        user.setNickName(SCIMv2ConnectorTestsUtils.VALUE_NICK_NAME + uid.toString().substring(0, 10));
        user.setName(new SCIMUserName());
        user.getName().setFamilyName(SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME);
        user.getName().setGivenName(SCIMv2ConnectorTestsUtils.VALUE_GIVEN_NAME);
        SCIMGenericComplex<EmailCanonicalType> email = new SCIMGenericComplex<>();
        email.setPrimary(true);
        email.setType(EmailCanonicalType.work);
        email.setValue(name);
        user.getEmails().add(email);
        SCIMGenericComplex<PhoneNumberCanonicalType> phone = new SCIMGenericComplex<>();
        phone.setPrimary(false);
        phone.setType(PhoneNumberCanonicalType.other);
        phone.setValue(SCIMv2ConnectorTestsUtils.VALUE_PHONE_NUMBER);
        user.getPhoneNumbers().add(phone);
        SCIMUserAddress userAddress = new SCIMUserAddress();
        userAddress.setStreetAddress("100 Universal City Plaza");
        userAddress.setLocality("Hollywood");
        userAddress.setRegion("CA");
        userAddress.setPostalCode("91608");
        userAddress.setCountry("US");
        userAddress.setPrimary(false);
        userAddress.setType(AddressCanonicalType.work);
        user.getAddresses().add(userAddress);
        if (PROPS.containsKey("auth.defaultEntitlement") && StringUtil.isNotBlank(
                PROPS.getProperty("auth.defaultEntitlement"))) {
            SCIMv2Entitlement entitlement = new SCIMv2Entitlement();
            entitlement.setType(SCIMAttributeUtils.SCIM_SCHEMA_TYPE_DEFAULT);
            entitlement.setValue(PROPS.getProperty("auth.defaultEntitlement"));
            user.getEntitlements().add(entitlement);
        }
        user.setActive(active);
        SCIMv2Attribute luckyNumberAttribute = new SCIMv2Attribute();
        luckyNumberAttribute.setExtensionSchema("urn:mem:params:scim:schemas:extension:LuckyNumberExtension");
        luckyNumberAttribute.setMutability(Mutability.readWrite);
        luckyNumberAttribute.setUniqueness(Uniqueness.server);
        luckyNumberAttribute.setName("luckyNumber");
        luckyNumberAttribute.setCaseExact(true);
        luckyNumberAttribute.setRequired(true);
        luckyNumberAttribute.setMultiValued(false);
        luckyNumberAttribute.setType("integer");
        luckyNumberAttribute.setReturned("default");
        user.getSCIMCustomAttributes().putIfAbsent(luckyNumberAttribute, Collections.singletonList("7"));

        SCIMv2EnterpriseUser enterpriseUser = new SCIMv2EnterpriseUser();
        enterpriseUser.setEmployeeNumber("11111");
        enterpriseUser.setManager(
                new SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager().displayName("amanager").value("22222"));
        user.setEnterpriseUser(enterpriseUser);

        SCIMv2User created = client.createUser(user);
        // group to user group1 = new SCIMv2Group.Builder()
        created.getGroups().addAll(groups);
        assertNotNull(created);
        assertNotNull(created.getId());
        LOG.info("Created user: {0}", created);

        return created;
    }

    private static SCIMv2Group createGroupServiceTest(final UUID uid, final SCIMv2Client client) {
        SCIMv2Group group = new SCIMv2Group();
        group.setId(uid.toString());
        group.setDisplayName("group_" + uid.toString().substring(0, 10));

        group = client.createGroup(group);
        assertNotNull(group);
        assertNotNull(group.getId());
        LOG.info("Created group: {0}", group);

        return group;
    }

    private static SCIMv2User updateUserServiceTest(final String userId, final SCIMv2Client client) {
        SCIMv2User user = client.getUser(userId);
        assertNotNull(user);
        assertNotNull(user.getName().getGivenName());
        assertFalse(user.getName().getGivenName().isEmpty());

        // want to update an attribute
        String oldGivenName = user.getName().getGivenName();
        String newGivenName = "Updated givenName";
        user.getName().setGivenName(newGivenName);

        // want also to remove attributes
        for (SCIMGenericComplex<PhoneNumberCanonicalType> phone : user.getPhoneNumbers()) {
            if (phone.getType().equals(PhoneNumberCanonicalType.other)) {
                // Note that "value" and "primary" must also be the same of current attribute in order to proceed
                // with deletion
                // See http://www.simplecloud.info/specs/draft-scim-api-01.html#edit-resource-with-patch
                break;
            }
        }

        // don't want to update addresses and emails
        user.getAddresses().clear();
        user.getEmails().clear();

        LOG.warn("Update user: {0}", user);
        SCIMv2User updated = client.updateUser(user);
        assertNotNull(updated);
        assertFalse(updated.getName().getGivenName().equals(oldGivenName));
        assertEquals(updated.getName().getGivenName(), newGivenName);
        LOG.info("Updated User with PATCH: {0}", updated);

        // test removed attribute
        for (SCIMGenericComplex<PhoneNumberCanonicalType> phone : updated.getPhoneNumbers()) {
            assertEquals(PhoneNumberCanonicalType.other, phone.getType());
        }

        return updated;
    }

    private static SCIMv2Group updateGroupServiceTest(final String groupId, final SCIMv2Client client) {
        SCIMv2Group group = client.getGroup(groupId);

        // want to update the displayName
        String oldDisplayName = group.getDisplayName();
        String newDisplayName = "Updated_" + oldDisplayName;
        group.setDisplayName(newDisplayName);

        LOG.warn("Update group: {0}", group);
        SCIMv2Group updated = client.updateGroup(group);
        assertNotNull(group);
        assertFalse(updated.getDisplayName().equals(oldDisplayName));
        assertEquals(updated.getDisplayName(), newDisplayName);
        LOG.info("Updated Group with PUT: {0}", updated);

        return updated;
    }

    private static SCIMv2User updateUserServiceTestPATCH(final String userId, final SCIMv2Client client)
            throws IllegalArgumentException {

        CONF.setUpdateUserMethod("PATCH");
        SCIMv2User user = client.getUser(userId);
        LOG.info("Updated User with PATCH: {0}", user);
        return user;
    }

    private static SCIMv2Group updateGroupServiceTestPATCH(final String groupId, final SCIMv2Client client)
            throws IllegalArgumentException {
        // TODO CONF.setUpdateMethod("PATCH");
        return null;
    }

    private static void readUsersServiceTest(final SCIMv2Client client) throws IllegalArgumentException {

        Set<String> attributesToGet = testUserAttributesToGet();

        // GET USER
        List<SCIMv2User> users = client.getAllUsers(attributesToGet);
        assertNotNull(users);
        assertFalse(users.isEmpty());
        LOG.info("Found Users: {0}", users);

        // GET USERS
        PagedResults<SCIMv2User> paged = client.getAllUsers(1, 1, attributesToGet);
        assertNotNull(paged);
        assertFalse(paged.getResources().isEmpty());
        assertTrue(paged.getResources().size() == 1);
        assertEquals(paged.getStartIndex(), 1);
        assertEquals(1, paged.getTotalResults());
        assertEquals(paged.getItemsPerPage(), 1);
        LOG.info("Paged Users: {0}", paged);

        PagedResults<SCIMv2User> paged2 = client.getAllUsers(2, 1, attributesToGet);
        assertNotNull(paged2);
        assertFalse(paged2.getResources().isEmpty());
        assertTrue(paged2.getResources().size() == 1);
        assertEquals(paged2.getStartIndex(), 2);
        assertEquals(1, paged2.getTotalResults());
        assertEquals(paged2.getItemsPerPage(), 1);
        LOG.info("Paged Users next page: {0}", paged2);
    }

    private static SCIMv2User readUserServiceTest(final String id, final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET USER
        SCIMv2User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes(user.getClass(), CONF);
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ADDRESS_WORK_STREET_ADDRESS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement") && StringUtil.isNotBlank(
                PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv2ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS));
        }

        // GET USER by userName
        List<SCIMv2User> users =
                client.getAllUsers(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME + " eq \"" + user.getUserName() + "\"",
                        testUserAttributesToGet());
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertNotNull(users.get(0).getId());
        LOG.info("Found User by userName: {0}", users.get(0));

        return user;
    }

    private static SCIMv2Group readGroupServiceTest(final String id, final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        SCIMv2Group group = client.getGroup(id);
        assertNotNull(group);
        assertNotNull(group.getId());
        LOG.info("Found Group: {0}", group);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = group.toAttributes(group.getClass(), CONF);
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes, SCIMv2ConnectorTestsUtils.DISPLAY_NAME));

        // search group by displayName
        List<SCIMv2Group> groups = client.getAllGroups(
                SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME + " eq \"" + group.getDisplayName() + "\"");
        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        assertNotNull(groups.get(0).getId());
        assertNotNull(groups.get(0).getDisplayName());
        LOG.info("Found Groups by displayName: {0}", groups.get(0));

        return group;
    }

    private static void readGroupsServiceTest(final SCIMv2Client client) throws IllegalArgumentException {

        List<SCIMv2Group> groups = client.getAllGroups();
        assertNotNull(groups);
        assertFalse(groups.isEmpty());
        LOG.info("Found Groups: {0}", groups);

        PagedResults<SCIMv2Group> paged = client.getAllGroups(1, 2);
        assertNotNull(paged);
        assertFalse(paged.getResources().isEmpty());
        assertEquals(2, paged.getResources().size());
        assertEquals(paged.getStartIndex(), 1);
        assertEquals(2, paged.getTotalResults());
        assertEquals(paged.getItemsPerPage(), 2);
        LOG.info("Paged Groups: {0}", paged);

        PagedResults<SCIMv2Group> paged2 = client.getAllGroups(3, 1);
        assertNotNull(paged2);
        assertFalse(paged2.getResources().isEmpty());
        assertEquals(3, paged2.getStartIndex());
        assertEquals(1, paged2.getItemsPerPage());
        LOG.info("Paged Groups next page: {0}", paged2);
    }

    private static void deleteUsersServiceTest(final SCIMv2Client client, final String username) {
        PagedResults<SCIMv2User> users =
                client.getAllUsers(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME + " sw \"" + username + "\"", 1, 100,
                        testUserAttributesToGet());
        assertNotNull(users);
        if (!users.getResources().isEmpty()) {
            for (SCIMv2User user : users.getResources()) {
                client.deleteUser(user.getId());
            }
        }
    }

    private static void deleteGroupsServiceTest(final SCIMv2Client client, final String displayNameInitials) {
        PagedResults<SCIMv2Group> groups =
                client.getAllGroups(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME + " sw \"" + displayNameInitials + "\"",
                        1, 100);
        assertNotNull(groups);
        if (!groups.getResources().isEmpty()) {
            for (SCIMv2Group group : groups.getResources()) {
                client.deleteGroup(group.getId());
            }
        }

        groups = client.getAllGroups(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME + " sw \"" + displayNameInitials + "\"",
                1, 100);

        assertTrue(groups.getResources().isEmpty());
    }

    private static void addCustomAttributes(final Set<Attribute> userAttrs) {
        if (testCustomAttributes()) {
            for (int i = 0; i < CUSTOM_ATTRIBUTES_VALUES.size(); i++) {
                userAttrs.add(AttributeBuilder.build(CUSTOM_ATTRIBUTES_KEYS.get(i), CUSTOM_ATTRIBUTES_VALUES.get(i)));
            }
        }
    }

    private static boolean testCustomAttributes() {
        return StringUtil.isNotBlank(CONF.getCustomAttributesJSON()) && !CUSTOM_ATTRIBUTES_KEYS.isEmpty()
                && !CUSTOM_ATTRIBUTES_VALUES.isEmpty() && !CUSTOM_ATTRIBUTES_UPDATE_VALUES.isEmpty();
    }

    private static Set<String> testUserAttributesToGet() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.ATTRIBUTE_ID);
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_PASSWORD);
        attributesToGet.add(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME);
        attributesToGet.add(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE);
        return attributesToGet;
    }

    @AfterEach
    public void cleanup() {
        // check that the user has effectively been removed
        try {
            ToListResultsHandler handler = new ToListResultsHandler();
            FACADE.search(ObjectClass.ACCOUNT, null, handler,
                    new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                            "displayName", "active",
                            "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
            handler.getObjects().forEach(
                    user -> FACADE.delete(ObjectClass.ACCOUNT, user.getUid(), new OperationOptionsBuilder().build()));
        } catch (NoSuchEntityException nsee) {
            LOG.ok(nsee, "No user found, as expected");
        }
    }

    @Test
    void validateWithProxyConfiguration() {
        try {
            // fail because port is null
            CONF.setProxyServerHost("localhost");
            CONF.setProxyServerUser("user");
            CONF.setProxyServerPassword("password");
            try {
                newFacade().validate();
                fail();
            } catch (Exception e) {
                assertTrue(e instanceof ConfigurationException);
                assertTrue(e.getMessage()
                        .contains("Proxy server type and port cannot be null or empty if host is specified."));
            }
            // fail because proxy server type is wrong
            CONF.setProxyServerHost("localhost");
            CONF.setProxyServerPort(8080);
            CONF.setProxyServerType("WRONG");
            try {
                newFacade().validate();
                fail();
            } catch (Exception e) {
                assertTrue(e instanceof ConfigurationException);
                assertTrue(e.getMessage()
                        .contains("Unsupported proxy Server type: WRONG"));
            }
            // fix proxy server type
            CONF.setProxyServerType(ProxyServerType.HTTP.value());
            CONF.setProxyServerUser("user");
            CONF.setProxyServerPassword(null);
            try {
                newFacade().validate();
                fail();
            } catch (Exception e) {
                assertTrue(e instanceof ConfigurationException);
                assertTrue(e.getMessage()
                        .contains("Proxy server password cannot be null or empty if user is specified."));
            }
        } finally {
            // cleanup not to invalidate other tests
            CONF.setProxyServerHost(null);
        }
    }
    
    @Test
    public void validate() {
        FACADE.validate();
    }

    @Test
    public void schema() {
        Schema schema = FACADE.schema();
        assertEquals(2, schema.getObjectClassInfo().size());

        boolean accountFound = false;
        boolean groupFound = false;
        for (ObjectClassInfo oci : schema.getObjectClassInfo()) {
            if (ObjectClass.ACCOUNT_NAME.equals(oci.getType())) {
                accountFound = true;
            }
            if (ObjectClass.GROUP_NAME.equals(oci.getType())) {
                groupFound = true;
            }
        }
        assertTrue(accountFound);
        assertTrue(groupFound);
    }

    @Test
    public void search() {
        // create some sample users
        SCIMv2User user1 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), newClient());
        SCIMv2User user2 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), newClient());
        SCIMv2User user3 = createUserServiceTest(UUID.randomUUID(), false, Collections.emptyList(), newClient());
        SCIMv2User user4 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), newClient());
        SCIMv2User user5 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), newClient());

        ToListResultsHandler handler = new ToListResultsHandler();

        SearchResult result = FACADE.search(ObjectClass.ACCOUNT, null, handler,
                new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                        "displayName", "active", "entitlements.default.value", "entitlements",
                        "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber",
                        SCIMv2EnterpriseUser.SCHEMA_URI + ".employeeNumber",
                        SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.value").build());
        assertNotNull(result);
        assertNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
        assertFalse(handler.getObjects().isEmpty());
        // verify keys
        assertTrue(handler.getObjects().stream().anyMatch(
                su -> user1.getUserName().equals(su.getName().getNameValue()) && "true".equalsIgnoreCase(
                su.getAttributeByName("active").getValue().get(0).toString()) && user1.getEmails().get(0)
                .getValue().equals(AttributeUtil.getAsStringValue(su.getAttributeByName("emails.work.value")))
                && user2.getName().getFamilyName()
                        .equals(AttributeUtil.getAsStringValue(su.getAttributeByName("name.familyName")))
                && 7 == AttributeUtil.getIntegerValue(
                        su.getAttributeByName("urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber"))
                && user1.getEnterpriseUser().getEmployeeNumber().equals(AttributeUtil.getAsStringValue(
                        su.getAttributeByName(SCIMv2EnterpriseUser.SCHEMA_URI + ".employeeNumber")))
                && user1.getEnterpriseUser().getManager().getValue().equals(AttributeUtil.getAsStringValue(
                        su.getAttributeByName(SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.value")))
                && !user1.getEntitlements().isEmpty()
                && su.getAttributeByName(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE)
                != null && su.getAttributeByName(
                        SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE).getValue()
                        .contains("00e09000000iZP5AAM")));
        assertTrue(handler.getObjects().stream().anyMatch(
                su -> user3.getUserName().equals(su.getName().getNameValue()) && "false".equalsIgnoreCase(
                su.getAttributeByName("active").getValue().get(0).toString())));
        assertTrue(
                handler.getObjects().stream().anyMatch(su -> user4.getUserName().equals(su.getName().getNameValue())));
        // verify attributes

        result = FACADE.search(ObjectClass.ACCOUNT, null, handler,
                new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                        "displayName", "active").setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

        result = FACADE.search(ObjectClass.ACCOUNT, null, handler,
                new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                        "displayName", "active").setPagedResultsOffset(2).setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
    }

    @Test
    public void pagedSearchUser() {
        // create some sample users for pagination
        createUser(UUID.randomUUID());
        createUser(UUID.randomUUID());
        createUser(UUID.randomUUID());
        createUser(UUID.randomUUID());
        createUser(UUID.randomUUID());

        final List<ConnectorObject> results = new ArrayList<>();
        final ResultsHandler handler = results::add;

        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        oob.setAttributesToGet("userName");
        oob.setPageSize(2);
        oob.setSortKeys(new SortKey("userName", false));

        FACADE.search(ObjectClass.ACCOUNT, null, handler, oob.build());

        assertEquals(2, results.size());

        results.clear();

        String cookie = "";
        do {
            oob.setPagedResultsCookie(cookie);
            final SearchResult searchResult = FACADE.search(ObjectClass.ACCOUNT, null, handler, oob.build());
            cookie = searchResult.getPagedResultsCookie();
        } while (cookie != null);

        LOG.info("Paged search results : {0}", results);

        assertTrue(results.size() > 3);
    }

    @Test
    public void crudUser() {
        SCIMv2Client client = newClient();

        UUID uid = UUID.randomUUID();

        String testUser;
        String testGroup1;
        try {
            // create group
            Uid group1 = createGroup(UUID.randomUUID(), "group1");
            Uid group2 = createGroup(UUID.randomUUID(), "group2");
            Uid group3 = createGroup(UUID.randomUUID(), "group3");

            SCIMv2Group createdGroup1 = readGroup(group1.getUidValue(), client);
            assertEquals(createdGroup1.getId(), group1.getUidValue());
            SCIMv2Group createdGroup2 = readGroup(group2.getUidValue(), client);
            assertEquals(createdGroup2.getId(), group2.getUidValue());
            SCIMv2Group createdGroup3 = readGroup(group3.getUidValue(), client);
            assertEquals(createdGroup3.getId(), group3.getUidValue());

            Uid created = createUser(uid, group1.getUidValue(), group2.getUidValue());
            testUser = created.getUidValue();

            SCIMv2User createdUser = readUser(testUser, client);
            assertEquals(createdUser.getId(), created.getUidValue());
            // check groups
            assertFalse(createdUser.getGroups().isEmpty());
            assertEquals(2, createdUser.getGroups().size());
            Optional<BaseResourceReference> groupRef1 =
                    createdUser.getGroups().stream().filter(g -> createdGroup1.getId().equals(g.getValue()))
                            .findFirst();
            // first group checks
            assertTrue(groupRef1.isPresent());
            assertEquals(createdGroup1.getDisplayName(), groupRef1.get().getDisplay());
            assertTrue(groupRef1.get().getRef().contains("Groups/" + createdGroup1.getId()));
            // second group checks
            Optional<BaseResourceReference> groupRef2 =
                    createdUser.getGroups().stream().filter(g -> createdGroup2.getId().equals(g.getValue()))
                            .findFirst();
            assertTrue(groupRef2.isPresent());
            assertEquals(createdGroup2.getDisplayName(), groupRef2.get().getDisplay());
            assertTrue(groupRef2.get().getRef().contains("/Groups/" + createdGroup2.getId()));
            // check entitlements
            assertTrue(createdUser.getEntitlements().stream().allMatch(e -> "00e09000000iZP5AAM".equals(e.getValue())));
            // read user through connector APIs
            ConnectorObject createdConnObj = FACADE.getObject(ObjectClass.ACCOUNT, created,
                    new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                            "displayName", "active", SCIMAttributeUtils.SCIM_USER_GROUPS,
                            "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
            Attribute groupsAttr = createdConnObj.getAttributeByName(SCIMAttributeUtils.SCIM_USER_GROUPS);
            assertNotNull(groupsAttr);
            assertTrue(groupsAttr.getValue().contains(group1.getUidValue()));
            assertTrue(groupsAttr.getValue().contains(group2.getUidValue()));

            Uid updated = "PATCH".equalsIgnoreCase(CONF.getUpdateGroupMethod())
                    ? updateUser(created, createdUser.getUserName(),
                            Arrays.asList(group1.getUidValue(), group3.getUidValue()),
                            Arrays.asList(group2.getUidValue()))
                    : updateUser(created, createdUser.getUserName(), group1.getUidValue(), group3.getUidValue());

            SCIMv2User updatedUser = readUser(updated.getUidValue(), client);
            LOG.info("Updated user: {0}", updatedUser);
            assertNull(updatedUser.getPassword()); // password won't be retrieved from API

            // check group update, remove group2, keep group1 and add group3
            assertEquals(2, updatedUser.getGroups().size());
            assertTrue(updatedUser.getGroups().stream().anyMatch(g -> g.getValue().equals(createdGroup1.getId())));
            assertTrue(updatedUser.getGroups().stream().anyMatch(g -> g.getValue().equals(createdGroup3.getId())));

            // test removed attribute
            SCIMv2User user = client.getUser(updatedUser.getId());
            assertNotNull(user);
            assertTrue(user.getPhoneNumbers().stream().noneMatch(pn -> PhoneNumberCanonicalType.other == pn.getType()));
            assertTrue(user.getPhoneNumbers().stream().anyMatch(
                    pn -> PhoneNumberCanonicalType.home == pn.getType() && pn.isPrimary() && "123456789".equals(
                    pn.getValue())));

            assertTrue(user.getEmails().stream().anyMatch(
                    email -> EmailCanonicalType.work == email.getType() && ("updated"
                    + updatedUser.getUserName()).equals(email.getValue())));

            // check delete user
            deleteUser(updated);
            assertThrows(NoSuchEntityException.class,
                    () -> FACADE.getObject(ObjectClass.ACCOUNT, updated, new OperationOptionsBuilder().build()));
        } catch (Exception e) {
            LOG.error(e, "While running crud test");
            fail(e.getMessage());
        } finally {
            // cleanup groups
            deleteGroupsServiceTest(client, "group");
        }
    }

    @Test
    public void searchGroup() {
        // create some sample users
        SCIMv2Client client = newClient();
        SCIMv2Group group1 = createGroupServiceTest(UUID.randomUUID(), client);
        SCIMv2Group group2 = createGroupServiceTest(UUID.randomUUID(), client);
        SCIMv2Group group3 = createGroupServiceTest(UUID.randomUUID(), client);
        SCIMv2Group group4 = createGroupServiceTest(UUID.randomUUID(), client);
        SCIMv2Group group5 = createGroupServiceTest(UUID.randomUUID(), client);

        ToListResultsHandler handler = new ToListResultsHandler();

        SearchResult result = FACADE.search(ObjectClass.GROUP, null, handler,
                new OperationOptionsBuilder().setAttributesToGet(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME).build());
        assertNotNull(result);
        assertNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
        assertFalse(handler.getObjects().isEmpty());
        assertTrue(handler.getObjects().stream().anyMatch(
                su -> group1.getDisplayName().equals(su.getName().getNameValue())
                && su.getAttributeByName(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME) != null
                && su.getAttributeByName(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME).getValue()
                        .contains(group1.getDisplayName())));
        assertTrue(handler.getObjects().stream()
                .anyMatch(su -> group2.getDisplayName().equals(su.getName().getNameValue())));

        result = FACADE.search(ObjectClass.GROUP, null, handler,
                new OperationOptionsBuilder().setAttributesToGet(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME)
                        .setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

        result = FACADE.search(ObjectClass.GROUP, null, handler,
                new OperationOptionsBuilder().setAttributesToGet(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME)
                        .setPagedResultsOffset(2).setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

    }

    @Test
    public void pagedSearchGroup() {
        // create some sample groups for pagination
        createGroup(UUID.randomUUID(), StringUtil.EMPTY);
        createGroup(UUID.randomUUID(), StringUtil.EMPTY);
        createGroup(UUID.randomUUID(), StringUtil.EMPTY);
        createGroup(UUID.randomUUID(), StringUtil.EMPTY);
        createGroup(UUID.randomUUID(), StringUtil.EMPTY);

        final List<ConnectorObject> results = new ArrayList<>();
        final ResultsHandler handler = results::add;

        final OperationOptionsBuilder oob = new OperationOptionsBuilder();
        oob.setPageSize(2);
        oob.setSortKeys(new SortKey("displayName", false));

        FACADE.search(ObjectClass.GROUP, null, handler, oob.build());

        assertEquals(2, results.size());

        results.clear();

        String cookie = "";
        do {
            oob.setPagedResultsCookie(cookie);
            final SearchResult searchResult = FACADE.search(ObjectClass.GROUP, null, handler, oob.build());
            cookie = searchResult.getPagedResultsCookie();
        } while (cookie != null);

        LOG.info("Paged search results : {0}", results);

        assertTrue(results.size() > 3);
    }

    @Test
    public void crudGroup() {
        SCIMv2Client client = newClient();

        try {
            // create group
            Uid group1 = createGroup(UUID.randomUUID(), StringUtil.EMPTY);

            SCIMv2Group createdGroup = readGroup(group1.getUidValue(), client);
            assertEquals(createdGroup.getId(), group1.getUidValue());
            assertNotNull(FACADE.getObject(ObjectClass.GROUP, new Uid(createdGroup.getId()),
                    new OperationOptionsBuilder().build()));

            Uid updated = updateGroup(group1, "updated_" + createdGroup.getDisplayName());
            SCIMv2Group updatedGroup = readGroup(updated.getUidValue(), client);
            assertEquals("updated_" + createdGroup.getDisplayName(), updatedGroup.getDisplayName());

            deleteGroup(updated);
            assertThrows(
                    NoSuchEntityException.class,
                    () -> FACADE.getObject(ObjectClass.GROUP, updated, new OperationOptionsBuilder().build()));
        } catch (Exception e) {
            LOG.error(e, "While running crud test");
            fail(e.getMessage());
        }
    }

    @Test
    public void serviceTestUser() {
        SCIMv2Client client = newClient();

        String testUser;
        try {
            SCIMv2User created1 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), client);
            SCIMv2User created2 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), client);
            SCIMv2User created3 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), client);
            SCIMv2User created4 = createUserServiceTest(UUID.randomUUID(), true, Collections.emptyList(), client);
            testUser = created1.getId();

            readUserServiceTest(testUser, client);

            readUsersServiceTest(client);

            updateUserServiceTest(testUser, client);

            updateUserServiceTestPATCH(testUser, newClient());

            deleteUsersServiceTest(client, created1.getUserName());
        } catch (Exception e) {
            LOG.error(e, "While running service test");
            fail(e.getMessage());
        }
    }

    @Test
    public void serviceTestGroup() {
        SCIMv2Client client = newClient();

        try {
            SCIMv2Group testGroup1 = createGroupServiceTest(UUID.randomUUID(), client);
            SCIMv2Group created2 = createGroupServiceTest(UUID.randomUUID(), client);

            readGroupServiceTest(testGroup1.getId(), client);

            readGroupsServiceTest(client);

            updateGroupServiceTest(testGroup1.getId(), client);

            updateGroupServiceTestPATCH(testGroup1.getId(), newClient());
        } catch (Exception e) {
            LOG.error(e, "While running service test");
            fail(e.getMessage());
        } finally {
            deleteGroupsServiceTest(client, "group_");
        }
    }
}
