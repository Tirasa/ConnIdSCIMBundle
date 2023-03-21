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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.SCIMComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUserAddress;
import net.tirasa.connid.bundles.scim.common.service.NoSuchEntityException;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMDefault;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMUserName;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Client;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class SCIMv11ConnectorTests {

    private static final Log LOG = Log.getLog(SCIMv11ConnectorTests.class);

    private static final Properties PROPS = new Properties();

    private static SCIMConnectorConfiguration CONF;

    private static SCIMv11Connector CONN;

    private static ConnectorFacade FACADE;

    private static final List<String> CUSTOMS_OTHER_SCHEMAS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_KEYS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_VALUES = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_UPDATE_VALUES = new ArrayList<>();

    @BeforeAll
    public static void setUpConf() throws IOException {
        PROPS.load(
                SCIMv11ConnectorTests.class.getResourceAsStream("/net/tirasa/connid/bundles/scim/authv11.properties"));

        Map<String, String> configurationParameters = new HashMap<>();
        for (final String name : PROPS.stringPropertyNames()) {
            configurationParameters.put(name, PROPS.getProperty(name));
        }
        CONF = SCIMv11ConnectorTestsUtils.buildConfiguration(configurationParameters);
        CONF.setUpdateMethod("PATCH");

        Boolean isValid = SCIMv11ConnectorTestsUtils.isConfigurationValid(CONF);
        if (isValid) {
            CONN = new SCIMv11Connector();
            CONN.init(CONF);
            try {
                CONN.test();
            } catch (Exception e) {
                LOG.error(e, "While testing connector");
            }
            CONN.schema();
        }

        // custom schemas
        if (PROPS.containsKey("auth.otherSchemas")
                && PROPS.getProperty("auth.otherSchemas") != null) {
            CUSTOMS_OTHER_SCHEMAS.addAll(
                    Arrays.asList(PROPS.getProperty("auth.otherSchemas").split("\\s*,\\s*")));
        }
        CUSTOMS_OTHER_SCHEMAS.add("urn:scim:schemas:core:1.0");

        // custom attributes
        if (PROPS.containsKey("auth.customAttributesValues")
                && PROPS.getProperty("auth.customAttributesValues") != null) {
            CUSTOM_ATTRIBUTES_VALUES.addAll(
                    Arrays.asList(PROPS.getProperty("auth.customAttributesValues").split("\\s*,\\s*")));
        }
        if (PROPS.containsKey("auth.customAttributesKeys")
                && PROPS.getProperty("auth.customAttributesKeys") != null) {
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
        assertNotNull(CONF.getUpdateMethod());
    }

    private static ConnectorFacade newFacade() {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        APIConfiguration impl = TestHelpers.createTestConfiguration(SCIMv11Connector.class, CONF);
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    private SCIMv11Client newClient() {
        return CONN.getClient();
    }

    @Test
    public void validate() {
        FACADE.validate();
    }

    @Test
    public void schema() {
        Schema schema = FACADE.schema();
        assertEquals(1, schema.getObjectClassInfo().size());

        boolean accountFound = false;
        for (ObjectClassInfo oci : schema.getObjectClassInfo()) {
            if (ObjectClass.ACCOUNT_NAME.equals(oci.getType())) {
                accountFound = true;
            }
        }
        assertTrue(accountFound);
    }

    @Test
    public void search() {
        ToListResultsHandler handler = new ToListResultsHandler();

        SearchResult result = FACADE.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().build());
        assertNotNull(result);
        assertNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
        assertFalse(handler.getObjects().isEmpty());

        result = FACADE.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

        result = FACADE.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().setPagedResultsOffset(2).setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
    }

    @Test
    public void pagedSearch() {
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

        assertTrue(results.size() > 2);
    }

    private void cleanup(
            final ConnectorFacade connector,
            final SCIMv11Client client,
            final String testUserUid) {
        if (testUserUid != null) {
            connector.delete(ObjectClass.ACCOUNT, new Uid(testUserUid), new OperationOptionsBuilder().build());
            try {
                client.deleteUser(testUserUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }

            try {
                client.getUser(testUserUid);
                fail(); // must fail
            } catch (NoSuchEntityException e) {
                assertNotNull(e);
            }
        }
    }

    private void cleanup(
            final SCIMv11Client client,
            final String testUserUid) {
        if (testUserUid != null) {
            client.deleteUser(testUserUid);

            try {
                client.getUser(testUserUid);
                fail(); // must fail
            } catch (ConnectorException e) {
                assertNotNull(e);
            }
        }
    }

    @Test
    public void crud() {
        SCIMv11Client client = newClient();

        String testUser = null;
        UUID uid = UUID.randomUUID();

        try {
            Uid created = createUser(uid);
            testUser = created.getUidValue();

            SCIMv11User createdUser = readUser(testUser, client);
            assertEquals(createdUser.getId(), created.getUidValue());

            Uid updated = updateUser(created);

            SCIMv11User updatedUser = readUser(updated.getUidValue(), client);
            LOG.info("Updated user: {0}", updatedUser);
            assertNull(updatedUser.getPassword()); // password won't be retrieved from API

            // test removed attribute
            SCIMv11User user = client.getUser(updatedUser.getId());
            assertNotNull(user);
            for (SCIMComplex<PhoneNumberCanonicalType> phone : user.getPhoneNumbers()) {
                assertNotEquals(phone.getType(), PhoneNumberCanonicalType.other);
            }
            for (SCIMComplex<EmailCanonicalType> email : user.getEmails()) {
                assertNotEquals(email.getType(), EmailCanonicalType.other);
                assertNotEquals(email.getType(), EmailCanonicalType.home);
            }
            assertTrue(user.getPhoneNumbers().isEmpty());
        } catch (Exception e) {
            LOG.error(e, "While running crud test");
            fail(e.getMessage());
        } finally {
            cleanup(FACADE, client, testUser);
        }
    }

    private Uid createUser(final UUID uid) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString(SCIMv11ConnectorTestsUtils.VALUE_PASSWORD.toCharArray()));
        String name = SCIMv11ConnectorTestsUtils.VALUE_USERNAME + uid.toString().substring(0, 10) + "@email.com";

        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME, name));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME,
                SCIMv11ConnectorTestsUtils.VALUE_FAMILY_NAME));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME,
                SCIMv11ConnectorTestsUtils.VALUE_NICK_NAME + uid.toString().substring(0, 10)));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE, name));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_VALUE,
                SCIMv11ConnectorTestsUtils.VALUE_PHONE_NUMBER));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_PRIMARY, false));
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE, true));
        userAttrs.add(password);

        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE,
                    PROPS.getProperty("auth.defaultEntitlement")));
        }

        // custom attributes
        addCustomAttributes(userAttrs);

        // custom schemas
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOMS_OTHER_SCHEMAS));

        Uid created = FACADE.create(ObjectClass.ACCOUNT, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(created);
        assertFalse(created.getUidValue().isEmpty());
        LOG.info("Created User uid: {0}", created);

        return created;
    }

    private Uid updateUser(final Uid created) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString((SCIMv11ConnectorTestsUtils.VALUE_PASSWORD + "01").toCharArray()));
        // UPDATE USER VALUE_PASSWORD
        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(password);

        // custom attributes
        addCustomAttributes(userAttrs);

        // want to remove an attribute
        // Note that "value" and "primary" must also be the same of current attribute in order to proceed with deletion
        // See http://www.simplecloud.info/specs/draft-scim-api-01.html#edit-resource-with-patch
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_OPERATION,
                "delete")); // will also set type to "other"
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_VALUE,
                SCIMv11ConnectorTestsUtils.VALUE_PHONE_NUMBER));
        userAttrs.add(AttributeBuilder.build(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_OTHER_PRIMARY,
                false));

        // custom schemas
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOMS_OTHER_SCHEMAS));

        Uid updated = FACADE.update(
                ObjectClass.ACCOUNT, created, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated User uid: {0}", updated);

        return updated;
    }

    private SCIMv11User readUser(final String id, final SCIMv11Client client)
            throws IllegalArgumentException, IllegalAccessException {
        SCIMv11User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(user.getName().getFamilyName(), SCIMv11ConnectorTestsUtils.VALUE_FAMILY_NAME);
        assertFalse(user.getEmails().isEmpty());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes(user.getClass());
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv11ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + "."));
        }

        if (testCustomAttributes()) {
            final List<ConnectorObject> found = new ArrayList<>();
            FACADE.search(ObjectClass.ACCOUNT,
                    new EqualsFilter(new Name(user.getUserName())),
                    found::add,
                    new OperationOptionsBuilder().setAttributesToGet(CUSTOM_ATTRIBUTES_KEYS).build());
            assertEquals(found.size(), 1);
            assertNotNull(found.get(0));
            assertNotNull(found.get(0).getName());
            for (String key : CUSTOM_ATTRIBUTES_KEYS) {
                assertNotNull(found.get(0).getAttributeByName(key));
                assertNotNull(found.get(0).getAttributeByName(key).getValue());
                assertFalse(found.get(0).getAttributeByName(key).getValue().isEmpty());
            }
            LOG.info("Found User using Connector search: {0}", found.get(0));
        }

        return user;
    }

    @Test
    public void serviceTest() {
        SCIMv11Client client = newClient();

        String testUser = null;
        UUID uid = UUID.randomUUID();

        try {
            deleteUsersServiceTest(client);

            SCIMv11User created = createUserServiceTest(uid, client);
            testUser = created.getId();

            readUserServiceTest(testUser, client);

            readUsersServiceTest(client);

            updateUserServiceTest(testUser, client);

            CONF.setUpdateMethod("PUT");
            updateUserServiceTestPUT(testUser, newClient());
        } catch (Exception e) {
            LOG.error(e, "While running service test");
            fail(e.getMessage());
        } finally {
            cleanup(client, testUser);
        }
    }

    private SCIMv11User createUserServiceTest(final UUID uid, final SCIMv11Client client) {
        SCIMv11User user = new SCIMv11User();
        String name = SCIMv11ConnectorTestsUtils.VALUE_USERNAME + uid.toString().substring(0, 10) + "@email.com";
        user.setUserName(name);
        user.setPassword(SCIMv11ConnectorTestsUtils.VALUE_PASSWORD);
        user.getSchemas().addAll(CUSTOMS_OTHER_SCHEMAS);
        user.setNickName(SCIMv11ConnectorTestsUtils.VALUE_NICK_NAME + uid.toString().substring(0, 10));
        user.setName(new SCIMUserName());
        user.getName().setFamilyName(SCIMv11ConnectorTestsUtils.VALUE_FAMILY_NAME);
        user.getName().setGivenName(SCIMv11ConnectorTestsUtils.VALUE_GIVEN_NAME);
        SCIMComplex<EmailCanonicalType> email = new SCIMComplex<>();
        email.setPrimary(true);
        email.setType(EmailCanonicalType.work);
        email.setValue(name);
        user.getEmails().add(email);
        SCIMComplex<PhoneNumberCanonicalType> phone = new SCIMComplex<>();
        phone.setPrimary(false);
        phone.setType(PhoneNumberCanonicalType.other);
        phone.setValue(SCIMv11ConnectorTestsUtils.VALUE_PHONE_NUMBER);
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
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            SCIMDefault entitlement = new SCIMDefault();
            entitlement.setValue(PROPS.getProperty("auth.defaultEntitlement"));
            user.getEntitlements().add(entitlement);
        }

        SCIMv11User created = client.createUser(user);
        assertNotNull(created);
        assertNotNull(created.getId());
        LOG.info("Created user: {0}", created);

        return created;
    }

    private SCIMv11User updateUserServiceTest(final String userId, final SCIMv11Client client) {
        SCIMv11User user = client.getUser(userId);
        assertNotNull(user);
        assertNotNull(user.getName().getGivenName());
        assertFalse(user.getName().getGivenName().isEmpty());

        // want to update an attribute
        String oldGivenName = user.getName().getGivenName();
        String newGivenName = "Updated givenName";
        user.getName().setGivenName(newGivenName);

        // want also to remove attributes
        for (SCIMComplex<PhoneNumberCanonicalType> phone : user.getPhoneNumbers()) {
            if (phone.getType().equals(PhoneNumberCanonicalType.other)) {
                // Note that "value" and "primary" must also be the same of current attribute in order to proceed with
                // deletion
                // See http://www.simplecloud.info/specs/draft-scim-api-01.html#edit-resource-with-patch
                phone.setOperation("delete");
                break;
            }
        }

        // don't want to update addresses and emails
        user.getAddresses().clear();
        user.getEmails().clear();

        LOG.warn("Update user: {0}", user);
        SCIMv11User updated = client.updateUser(user);
        assertNotNull(updated);
        assertFalse(updated.getName().getGivenName().equals(oldGivenName));
        assertEquals(updated.getName().getGivenName(), newGivenName);
        LOG.info("Updated User with PATCH: {0}", updated);

        // test removed attribute
        for (SCIMComplex<PhoneNumberCanonicalType> phone : updated.getPhoneNumbers()) {
            assertNotEquals(phone.getType(), PhoneNumberCanonicalType.other);
        }

        return updated;
    }

    private SCIMv11User updateUserServiceTestPUT(final String userId, final SCIMv11Client client)
            throws IllegalArgumentException, IllegalAccessException {
        SCIMv11User user = client.getUser(userId);
        assertNotNull(user);
        assertNotNull(user.getNickName());
        assertFalse(user.getNickName().isEmpty());

        // want to update an attribute
        String oldName = user.getNickName();
        String newName = "Updated nickname" + UUID.randomUUID().toString().substring(0, 10);
        user.setNickName(newName);
        user.setMeta(null); // no need

        // 'formatted' filed is read-only
        user.getAddresses().get(0).setFormatted(null);

        // custom attributes
        if (testCustomAttributes()) {
            Set<Attribute> userAttrs = new HashSet<>();
            for (Map.Entry<String, List<Object>> entry : user.getReturnedCustomAttributes().entrySet()) {
                userAttrs.add(AttributeBuilder.build(entry.getKey(), entry.getValue()));
            }
            user.fillSCIMCustomAttributes(userAttrs, CONF.getCustomAttributesJSON());
        }

        SCIMv11User updated = client.updateUser(user);
        assertNotNull(updated);
        assertFalse(updated.getNickName().equals(oldName));
        assertEquals(updated.getNickName(), newName);
        LOG.info("Updated User with PUT: {0}", updated);

        return updated;
    }

    private void readUsersServiceTest(final SCIMv11Client client)
            throws IllegalArgumentException, IllegalAccessException {
        Set<String> attributesToGet = testAttributesToGet();

        // GET USER
        List<SCIMv11User> users = client.getAllUsers(attributesToGet);
        assertNotNull(users);
        assertFalse(users.isEmpty());
        LOG.info("Found Users: {0}", users);

        // GET USERS
        PagedResults<SCIMv11User> paged = client.getAllUsers(1, 1, attributesToGet);
        assertNotNull(paged);
        assertFalse(paged.getResources().isEmpty());
        assertTrue(paged.getResources().size() == 1);
        assertEquals(paged.getStartIndex(), 1);
        assertNotEquals(paged.getTotalResults(), 1);
        assertEquals(paged.getItemsPerPage(), 1);
        LOG.info("Paged Users: {0}", paged);

        PagedResults<SCIMv11User> paged2 = client.getAllUsers(2, 1, attributesToGet);
        assertNotNull(paged2);
        assertFalse(paged2.getResources().isEmpty());
        assertTrue(paged2.getResources().size() == 1);
        assertEquals(paged2.getStartIndex(), 2);
        assertNotEquals(paged2.getTotalResults(), 1);
        assertEquals(paged2.getItemsPerPage(), 1);
        LOG.info("Paged Users next page: {0}", paged2);
    }

    private SCIMv11User readUserServiceTest(final String id, final SCIMv11Client client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET USER
        SCIMv11User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes(user.getClass());
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_ADDRESS_WORK_STREET_ADDRESS));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv11ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv11ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + "."));
        }

        // GET USER by userName
        List<SCIMv11User> users = client.getAllUsers(
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME
                + " eq \"" + user.getUserName() + "\"", testAttributesToGet());
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertNotNull(users.get(0).getId());
        LOG.info("Found User by userName: {0}", users.get(0));

        return user;
    }

    private void deleteUsersServiceTest(final SCIMv11Client client) {
        PagedResults<SCIMv11User> users = client.getAllUsers(
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME
                + " sw \"" + SCIMv11ConnectorTestsUtils.VALUE_USERNAME + "\"", 1, 100, testAttributesToGet());
        assertNotNull(users);
        if (!users.getResources().isEmpty()) {
            for (SCIMv11User user : users.getResources()) {
                client.deleteUser(user.getId());
            }
        }
    }

    private void addCustomAttributes(final Set<Attribute> userAttrs) {
        if (testCustomAttributes()) {
            for (int i = 0; i < CUSTOM_ATTRIBUTES_VALUES.size(); i++) {
                userAttrs.add(AttributeBuilder.build(
                        CUSTOM_ATTRIBUTES_KEYS.get(i),
                        CUSTOM_ATTRIBUTES_VALUES.get(i)));
            }
        }
    }

    private boolean testCustomAttributes() {
        return StringUtil.isNotBlank(CONF.getCustomAttributesJSON())
                && !CUSTOM_ATTRIBUTES_KEYS.isEmpty()
                && !CUSTOM_ATTRIBUTES_VALUES.isEmpty()
                && !CUSTOM_ATTRIBUTES_UPDATE_VALUES.isEmpty();
    }

    private Set<String> testAttributesToGet() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_ID);
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_PASSWORD);
        attributesToGet.add(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME);
        attributesToGet.add(SCIMv11ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE);
        return attributesToGet;
    }

}
