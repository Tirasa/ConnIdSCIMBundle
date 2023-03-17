/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.v2;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.*;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.SCIMComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUserAddress;
import net.tirasa.connid.bundles.scim.common.service.NoSuchEntityException;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMUserName;
import net.tirasa.connid.bundles.scim.v11.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.v2.dto.Mutability;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.dto.Uniqueness;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.apache.commons.lang3.BooleanUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.objects.*;
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

    private final static Properties PROPS = new Properties();

    private static SCIMConnectorConfiguration CONF;

    private static SCIMv2Connector CONN;

    private static ConnectorFacade connector;

    private static final List<String> CUSTOMS_OTHER_SCHEMAS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_KEYS = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_VALUES = new ArrayList<>();

    private static final List<String> CUSTOM_ATTRIBUTES_UPDATE_VALUES = new ArrayList<>();

    @Container
    private static final GenericContainer<?> scimpleServerContainer =
            new GenericContainer<>("tirasa/scimple-server:1.0.0")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forLogMessage(".*Started ScimpleSpringBootApplication in.*\\n", 1));

    @BeforeAll
    public static void setUpConf() throws IOException {
        PROPS.load(
                SCIMv2ConnectorTests.class.getResourceAsStream("/net/tirasa/connid/bundles/scim/authv2.properties"));

        Map<String, String> configurationParameters = new HashMap<>();
        for (final String name : PROPS.stringPropertyNames()) {
            configurationParameters.put(name, PROPS.getProperty(name));
        }
        CONF = SCIMv2ConnectorTestsUtils.buildConfiguration(configurationParameters,
                scimpleServerContainer.getFirstMappedPort());

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
        if (PROPS.containsKey("auth.otherSchemas")
                && PROPS.getProperty("auth.otherSchemas") != null) {
            CUSTOMS_OTHER_SCHEMAS.addAll(
                    Arrays.asList(PROPS.getProperty("auth.otherSchemas").split("\\s*,\\s*")));
        }
        CUSTOMS_OTHER_SCHEMAS.add("urn:ietf:params:scim:schemas:core:2.0:User");

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

        connector = newFacade();

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
        APIConfiguration impl = TestHelpers.createTestConfiguration(SCIMv2Connector.class, CONF);
        impl.getResultsHandlerConfiguration().setFilteredResultsHandlerInValidationMode(true);
        return factory.newInstance(impl);
    }

    private SCIMv2Client newClient() {
        return CONN.getClient();
    }

    @Test
    public void validate() {
        connector.validate();
    }

    @Test
    public void schema() {
        Schema schema = connector.schema();
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
        // create some sample users
        SCIMv2User user1 = createUserServiceTest(UUID.randomUUID(), true, newClient());
        SCIMv2User user2 = createUserServiceTest(UUID.randomUUID(), true, newClient());
        SCIMv2User user3 = createUserServiceTest(UUID.randomUUID(), false, newClient());
        SCIMv2User user4 = createUserServiceTest(UUID.randomUUID(), true, newClient());
        SCIMv2User user5 = createUserServiceTest(UUID.randomUUID(), true, newClient());

        ToListResultsHandler handler = new ToListResultsHandler();

        SearchResult result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                        "displayName", "active",
                        "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
        assertNotNull(result);
        assertNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
        assertFalse(handler.getObjects().isEmpty());
        // verify keys
        assertTrue(handler.getObjects().stream().anyMatch(su -> user1.getUserName().equals(su.getName().getNameValue())
                && BooleanUtils.toBoolean(su.getAttributeByName("active").getValue().get(0).toString())
                && user1.getEmails().get(0).getValue().equals(
                AttributeUtil.getAsStringValue(su.getAttributeByName("emails.work.value")))
                && user2.getName().getFamilyName().equals(
                AttributeUtil.getAsStringValue(su.getAttributeByName("name.familyName")))
                && 7 == AttributeUtil.getIntegerValue(su.getAttributeByName(
                "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber"))));
        assertTrue(handler.getObjects().stream().anyMatch(su -> user3.getUserName().equals(su.getName().getNameValue())
                && !BooleanUtils.toBoolean(su.getAttributeByName("active").getValue().get(0).toString())));
        assertTrue(
                handler.getObjects().stream().anyMatch(su -> user4.getUserName().equals(su.getName().getNameValue())));
        // verify attributes

        result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder()
                        .setAttributesToGet("name", "emails.work.value", "name.familyName", "displayName", "active")
                        .setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());

        result = connector.search(ObjectClass.ACCOUNT,
                null,
                handler,
                new OperationOptionsBuilder()
                        .setAttributesToGet("name", "emails.work.value", "name.familyName",
                                "displayName", "active")
                        .setPagedResultsOffset(2).setPageSize(1).build());
        assertNotNull(result);
        assertNotNull(result.getPagedResultsCookie());
        assertEquals(-1, result.getRemainingPagedResults());
    }

    @Test
    public void pagedSearch() {
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

        connector.search(ObjectClass.ACCOUNT, null, handler, oob.build());

        assertEquals(2, results.size());

        results.clear();

        String cookie = "";
        do {
            oob.setPagedResultsCookie(cookie);
            final SearchResult searchResult = connector.search(ObjectClass.ACCOUNT, null, handler, oob.build());
            cookie = searchResult.getPagedResultsCookie();
        } while (cookie != null);

        LOG.info("Paged search results : {0}", results);

        assertTrue(results.size() > 3);
    }

    @Test
    public void crud() {
        SCIMv2Client client = newClient();

        String testUser = null;
        UUID uid = UUID.randomUUID();

        try {
            Uid created = createUser(uid);
            testUser = created.getUidValue();

            SCIMv2User createdUser = readUser(testUser, client);
            assertEquals(createdUser.getId(), created.getUidValue());

            Uid updated = updateUser(created, createdUser.getUserName());

            SCIMv2User updatedUser = readUser(updated.getUidValue(), client);
            LOG.info("Updated user: {0}", updatedUser);
            assertNull(updatedUser.getPassword()); // password won't be retrieved from API

            // test removed attribute
            SCIMv2User user = client.getUser(updatedUser.getId());
            assertNotNull(user);
            assertTrue(user.getPhoneNumbers().stream()
                    .noneMatch(pn -> PhoneNumberCanonicalType.other == pn.getType()));
            assertTrue(user.getPhoneNumbers().stream()
                    .anyMatch(
                            pn -> PhoneNumberCanonicalType.home == pn.getType() && pn.isPrimary() && "123456789".equals(
                                    pn.getValue())));

            assertTrue(user.getEmails().stream()
                    .anyMatch(email -> EmailCanonicalType.work == email.getType()
                            && ("updated" + updatedUser.getUserName()).equals(email.getValue())));
        } catch (Exception e) {
            LOG.error(e, "While running crud test");
            fail(e.getMessage());
        }
    }

    private Uid createUser(final UUID uid) {
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

        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            userAttrs.add(
                    AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE,
                            PROPS.getProperty("auth.defaultEntitlement")));
        }

        // custom attributes
        addCustomAttributes(userAttrs);

        // custom schemas
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOMS_OTHER_SCHEMAS));

        Uid created = connector.create(ObjectClass.ACCOUNT, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(created);
        assertFalse(created.getUidValue().isEmpty());
        LOG.info("Created User uid: {0}", created);

        return created;
    }

    private Uid updateUser(final Uid created, final String name) {
        Attribute password = AttributeBuilder.buildPassword(
                new GuardedString((SCIMv2ConnectorTestsUtils.VALUE_PASSWORD + "01").toCharArray()));
        // UPDATE USER VALUE_PASSWORD
        Set<Attribute> userAttrs = new HashSet<>();
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME, name));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME,
                SCIMv2ConnectorTestsUtils.VALUE_NICK_NAME + created.getUidValue().substring(0, 10)));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE,
                "updated" + name));
        // no phone number -> delete
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_HOME_VALUE,
                "123456789"));
        userAttrs.add(AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_PHONE_HOME_PRIMARY, true));
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE, true));
        userAttrs.add(password);

        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            userAttrs.add(
                    AttributeBuilder.build(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE,
                            PROPS.getProperty("auth.defaultEntitlement")));
        }

        // custom attributes
        addCustomAttributes(userAttrs);

        // custom schemas
        userAttrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_SCHEMAS, CUSTOMS_OTHER_SCHEMAS));

        Uid updated = connector.update(
                ObjectClass.ACCOUNT, created, userAttrs, new OperationOptionsBuilder().build());
        assertNotNull(updated);
        assertFalse(updated.getUidValue().isEmpty());
        LOG.info("Updated User uid: {0}", updated);

        return updated;
    }

    private SCIMv2User readUser(final String id, final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        SCIMv2User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(user.getName().getFamilyName(), SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME);
        assertFalse(user.getEmails().isEmpty());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes();
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
//        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
//                SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv2ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + "."));
        }

        if (testCustomAttributes()) {
            List<ConnectorObject> found = new ArrayList<>();
            connector.search(ObjectClass.ACCOUNT,
                    new EqualsFilter(new Name(user.getUserName())),
                    found::add,
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

        return user;
    }

    @Test
    public void serviceTest() {
        SCIMv2Client client = newClient();

        String testUser = null;

        try {
            SCIMv2User created1 = createUserServiceTest(UUID.randomUUID(), true, client);
            SCIMv2User created2 = createUserServiceTest(UUID.randomUUID(), true, client);
            SCIMv2User created3 = createUserServiceTest(UUID.randomUUID(), true, client);
            SCIMv2User created4 = createUserServiceTest(UUID.randomUUID(), true, client);
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

    @AfterEach
    public void cleanup() {
        // check that the user has effectively been removed
        try {
            ToListResultsHandler handler = new ToListResultsHandler();
            connector.search(ObjectClass.ACCOUNT,
                    null,
                    handler,
                    new OperationOptionsBuilder().setAttributesToGet("name", "emails.work.value", "name.familyName",
                            "displayName", "active",
                            "urn:mem:params:scim:schemas:extension:LuckyNumberExtension.luckyNumber").build());
            handler.getObjects().forEach(user ->
                    connector.delete(ObjectClass.ACCOUNT, user.getUid(), new OperationOptionsBuilder().build()));
        } catch (NoSuchEntityException nsee) {
            nsee.printStackTrace();
        }
    }

    private SCIMv2User createUserServiceTest(final UUID uid, final boolean active, final SCIMv2Client client) {
        SCIMv2User user = new SCIMv2User();
        String name = SCIMv2ConnectorTestsUtils.VALUE_USERNAME + uid.toString().substring(0, 10) + "@email.com";
        user.setUserName(name);
        user.setPassword(SCIMv2ConnectorTestsUtils.VALUE_PASSWORD);
        user.getSchemas().addAll(CUSTOMS_OTHER_SCHEMAS);
        user.setNickName(SCIMv2ConnectorTestsUtils.VALUE_NICK_NAME + uid.toString().substring(0, 10));
        user.setName(new SCIMUserName());
        user.getName().setFamilyName(SCIMv2ConnectorTestsUtils.VALUE_FAMILY_NAME);
        user.getName().setGivenName(SCIMv2ConnectorTestsUtils.VALUE_GIVEN_NAME);
        SCIMComplex<EmailCanonicalType> email = new SCIMComplex<>();
        email.setPrimary(true);
        email.setType(EmailCanonicalType.work);
        email.setValue(name);
        user.getEmails().add(email);
        SCIMComplex<PhoneNumberCanonicalType> phone = new SCIMComplex<>();
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
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            SCIMComplex<String> entitlement = new SCIMComplex<>();
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

        SCIMv2User created = client.createUser(user);
        assertNotNull(created);
        assertNotNull(created.getId());
        LOG.info("Created user: {0}", created);

        return created;
    }

    private SCIMv2User updateUserServiceTest(final String userId, final SCIMv2Client client) {
        SCIMv2User user = client.getUser(userId);
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
                // Note that "value" and "primary" must also be the same of current attribute in order to proceed with deletion
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
        for (SCIMComplex<PhoneNumberCanonicalType> phone : updated.getPhoneNumbers()) {
            assertEquals(PhoneNumberCanonicalType.other, phone.getType());
        }

        return updated;
    }

    private SCIMv2User updateUserServiceTestPATCH(final String userId, final SCIMv2Client client)
            throws IllegalArgumentException {
        CONF.setUpdateMethod("PATCH");
        SCIMv2User user = client.getUser(userId);
        LOG.info("Updated User with PATCH: {0}", user);
        return user;
    }

    private void readUsersServiceTest(final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        Set<String> attributesToGet = testAttributesToGet();

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

    private SCIMv2User readUserServiceTest(final String id, final SCIMv2Client client)
            throws IllegalArgumentException, IllegalAccessException {
        // GET USER
        SCIMv2User user = client.getUser(id);
        assertNotNull(user);
        assertNotNull(user.getId());
        LOG.info("Found User: {0}", user);

        // USER TO ATTRIBUTES
        Set<Attribute> toAttributes = user.toAttributes();
        LOG.info("User to attributes: {0}", toAttributes);
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_NICK_NAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_ADDRESS_WORK_STREET_ADDRESS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.SCIM_USER_SCHEMAS));
        assertTrue(SCIMv2ConnectorTestsUtils.hasAttribute(toAttributes,
                SCIMAttributeUtils.USER_ATTRIBUTE_ACTIVE));
        if (PROPS.containsKey("auth.defaultEntitlement")
                && StringUtil.isNotBlank(PROPS.getProperty("auth.defaultEntitlement"))) {
            assertTrue(SCIMv2ConnectorTestsUtils.containsAttribute(toAttributes,
                    SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + "."));
        }

        // GET USER by userName
        List<SCIMv2User> users = client.getAllUsers(
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME
                        + " eq \"" + user.getUserName() + "\"", testAttributesToGet());
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertNotNull(users.get(0).getId());
        LOG.info("Found User by userName: {0}", users.get(0));

        return user;
    }

    private void deleteUsersServiceTest(final SCIMv2Client client, final String username) {
        PagedResults<SCIMv2User> users = client.getAllUsers(
                SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME
                        + " sw \"" + username + "\"", 1, 100, testAttributesToGet());
        assertNotNull(users);
        if (!users.getResources().isEmpty()) {
            for (SCIMv2User user : users.getResources()) {
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
        attributesToGet.add(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_FAMILY_NAME);
        attributesToGet.add(SCIMv2ConnectorTestsUtils.USER_ATTRIBUTE_EMAIL_WORK_VALUE);
        return attributesToGet;
    }

}
