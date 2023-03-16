package net.tirasa.connid.bundles.scim.common;

import java.util.*;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.common.service.SCIMService;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.*;

public abstract class AbstractSCIMConnector<T extends SCIMUser<Attribute, ? extends SCIMBaseMeta>, ST extends SCIMService>
        implements Connector, CreateOp, DeleteOp, SchemaOp, SearchOp<Filter>, TestOp, UpdateOp {

    private static final Log LOG = Log.getLog(AbstractSCIMConnector.class);

    protected SCIMConnectorConfiguration configuration;

    protected ST client;

    @Override
    public void init(final Configuration configuration) {
        LOG.ok("Init");

        this.configuration = (SCIMConnectorConfiguration) configuration;
        this.configuration.validate();
        // check whether PATCH is supported by the SCIM server, if so enable it in the configuration

        client = buildSCIMClient(SCIMConnectorConfiguration.class.cast(configuration));

        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    @Override
    public void dispose() {
        LOG.ok("Configuration cleanup");

        configuration = null;
    }

    @Override
    public void executeQuery(final ObjectClass objectClass, final Filter query, final ResultsHandler handler,
                             final OperationOptions options) {
        LOG.ok("Connector READ");

        Attribute key = null;
        if (query instanceof EqualsFilter) {
            Attribute filterAttr = ((EqualsFilter) query).getAttribute();
            if (filterAttr instanceof Uid
                    || ObjectClass.ACCOUNT.equals(objectClass)
                    || ObjectClass.GROUP.equals(objectClass)) {
                key = filterAttr;
            }
        }

        Set<String> attributesToGet = new HashSet<>();
        if (options.getAttributesToGet() != null) {
            attributesToGet.addAll(Arrays.asList(options.getAttributesToGet()));
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            if (key == null) {
                List<T> users = null;
                int remainingResults = -1;
                int pagesSize = options.getPageSize() == null ? -1 : options.getPageSize();
                String cookie = options.getPagedResultsCookie();

                try {
                    if (pagesSize != -1) {
                        if (StringUtil.isNotBlank(cookie)) {
                            PagedResults<T> pagedResult =
                                    client.getAllUsers(Integer.valueOf(cookie), pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize
                                    ? String.valueOf(pagedResult.getStartIndex() + users.size())
                                    : null;
                        } else {
                            PagedResults<T> pagedResult = client.getAllUsers(1, pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize
                                    ? String.valueOf(pagedResult.getStartIndex() + users.size())
                                    : null;
                        }
                    } else {
                        users = client.getAllUsers(attributesToGet);
                    }
                } catch (Exception e) {
                    SCIMUtils.wrapGeneralError("While getting Users!", e);
                }

                for (T user : users) {
                    handler.handle(fromUser(user, attributesToGet));
                }

                if (handler instanceof SearchResultsHandler) {
                    ((SearchResultsHandler) handler).handleResult(new SearchResult(cookie, remainingResults));
                }
            } else {
                T result = null;
                if (Uid.NAME.equals(key.getName()) || SCIMAttributeUtils.USER_ATTRIBUTE_ID.equals(key.getName())) {
                    result = null;
                    try {
                        result = (T) client.getUser(AttributeUtil.getAsStringValue(key));
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError("While getting User : "
                                + key.getName() + " - " + AttributeUtil.getAsStringValue(key), e);
                    }
                } else {
                    try {
                        List<T> users =
                                client.getAllUsers((Name.NAME.equals(key.getName()) ? "username" : key.getName()) +
                                        " eq \"" + AttributeUtil.getAsStringValue(key) + "\"", attributesToGet);
                        if (!users.isEmpty()) {
                            result = users.get(0);
                        }
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError("While getting User : "
                                + key.getName() + " - " + AttributeUtil.getAsStringValue(key), e);
                    }
                }
                if (result != null) {
                    handler.handle(fromUser(result, attributesToGet));
                }
            }
        } else {
            LOG.warn("Search of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException("Search of type" + objectClass.getObjectClassValue()
                    + " is not supported");
        }
    }

    @Override
    public Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes,
                      final OperationOptions options) {
        LOG.ok("Connector CREATE");

        if (createAttributes == null || createAttributes.isEmpty()) {
            SCIMUtils.handleGeneralError("Set of Attributes value is null or empty");
        }

        final AttributesAccessor accessor = new AttributesAccessor(createAttributes);

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            T user = buildNewUserEntity();
            String username = accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
            if (username == null) {
                username = accessor.findString(Name.NAME);
            }
            String externalId = accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_EXTERNAL_ID);

            GuardedString password = accessor.findGuardedString(OperationalAttributes.PASSWORD_NAME);
            Attribute status = accessor.find(OperationalAttributes.ENABLE_NAME);

            try {
                user.setUserName(username);
                user.setExternalId(externalId != null ? externalId : username);

                if (password == null) {
                    LOG.warn("Missing password attribute");
                } else {
                    user.setPassword(SecurityUtil.decrypt(password));
                }

                if (status == null
                        || status.getValue() == null
                        || status.getValue().isEmpty()) {
                    LOG.warn("{0} attribute value not correct or not found, won't handle User status",
                            OperationalAttributes.ENABLE_NAME);
                } else {
                    user.setActive(Boolean.parseBoolean(status.getValue().get(0).toString()));
                }

                user.fromAttributes(createAttributes);

                // custom attributes
                if (StringUtil.isNotBlank(
                        configuration.getCustomAttributesJSON())) {
                    user.fillSCIMCustomAttributes(createAttributes,
                            configuration.getCustomAttributesJSON());
                }

                client.createUser(user);
            } catch (Exception e) {
                SCIMUtils.wrapGeneralError("Could not create User : " + username, e);
            }

            return new Uid(user.getId());

        } else {
            LOG.warn("Create of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException("Create of type" + objectClass.getObjectClassValue()
                    + " is not supported");
        }
    }

    @Override
    public Uid update(final ObjectClass objectClass, final Uid uid, final Set<Attribute> replaceAttributes,
                      final OperationOptions options) {
        LOG.ok("Connector UPDATE object [{0}]", uid);

        if (replaceAttributes == null || replaceAttributes.isEmpty()) {
            SCIMUtils.handleGeneralError("Set of Attributes value is null or empty");
        }

        final AttributesAccessor accessor = new AttributesAccessor(replaceAttributes);

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            Uid returnUid = uid;

            Attribute status = accessor.find(OperationalAttributes.ENABLE_NAME);
            String username = accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
            if (username == null) {
                username = accessor.findString(Name.NAME);
            }

            T user = buildNewUserEntity();
            user.setId(uid.getUidValue());
            user.setUserName(username);

            if (status == null
                    || status.getValue() == null
                    || status.getValue().isEmpty()) {
                LOG.warn("{0} attribute value not correct, can't handle User  status update",
                        OperationalAttributes.ENABLE_NAME);
            } else {
                user.setActive(Boolean.parseBoolean(status.getValue().get(0).toString()));
            }

            // custom attributes
            if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
                user.fillSCIMCustomAttributes(replaceAttributes, configuration.getCustomAttributesJSON());
            }

            try {
                user.fromAttributes(replaceAttributes);

                // password
                GuardedString password = accessor.getPassword() != null
                        ? accessor.getPassword()
                        : accessor.findGuardedString(OperationalAttributes.PASSWORD_NAME);
                if (password == null) {
                    LOG.info("No password to update");
                } else {
                    String decryptedPassword = SecurityUtil.decrypt(password);
                    user.setPassword(decryptedPassword);
                }

                client.updateUser(user);

                returnUid = new Uid(user.getId());
            } catch (Exception e) {
                SCIMUtils.wrapGeneralError(
                        "Could not update User " + uid.getUidValue() + " from attributes ", e);
            }

            return returnUid;

        } else {
            LOG.warn("Update of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException("Update of type" + objectClass.getObjectClassValue()
                    + " is not supported");
        }
    }

    @Override
    public void delete(final ObjectClass objectClass, final Uid uid, final OperationOptions options) {
        LOG.ok("Connector DELETE object [{0}]", uid);

        if (StringUtil.isBlank(uid.getUidValue())) {
            LOG.error("Uid not provided or empty ");
            throw new InvalidAttributeValueException("Uid value not provided or empty");
        }

        if (objectClass == null) {
            LOG.error("Object value not provided {0} ", objectClass);
            throw new InvalidAttributeValueException("Object value not provided");
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            try {
                client.deleteUser(uid.getUidValue());
            } catch (Exception e) {
                SCIMUtils.wrapGeneralError("Could not delete User " + uid.getUidValue(), e);
            }

        } else {
            LOG.warn("Delete of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException("Delete of type" + objectClass.getObjectClassValue()
                    + " is not supported");
        }
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(
            final ObjectClass objectClass,
            final OperationOptions options) {
        return filter -> Collections.singletonList(filter);
    }

    @Override
    public void test() {
        LOG.ok("Connector TEST");

        if (configuration != null) {
            if (client != null && client.testService()) {
                LOG.ok("Test was successfull");
            } else {
                SCIMUtils.handleGeneralError("Test error. Problems with client service");
            }
        } else {
            LOG.error("Test error. No instance of the configuration class");
        }
    }

    protected ConnectorObject fromUser(final T user, final Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(ObjectClass.ACCOUNT);
        builder.setUid(user.getId());
        builder.setName(user.getUserName());

        try {
            Set<Attribute> userAttributes = user.toAttributes();

            for (Attribute toAttribute : userAttributes) {
                String attributeName = toAttribute.getName();
                for (String attributeToGetName : attributesToGet) {
                    if (attributeName.equals(attributeToGetName)) {
                        builder.addAttribute(toAttribute);
                        break;
                    }
                }
            }

            // custom attributes
            if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
                for (String customAttributeKey : user.getReturnedCustomAttributes().keySet()) {
                    builder.addAttribute(customAttributeKey,
                            user.getReturnedCustomAttributes().get(customAttributeKey));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOG.error(ex, "While converting to attributes");
        }

        return builder.build();
    }

    protected abstract T buildNewUserEntity();

    protected abstract ST getClient();

    protected abstract ST buildSCIMClient(SCIMConnectorConfiguration configuration);

}
