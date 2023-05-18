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
package net.tirasa.connid.bundles.scim.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.PagedResults;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBasePatch;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.Type;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public abstract class AbstractSCIMService<UT extends SCIMUser<? extends SCIMBaseMeta, ? extends SCIMEnterpriseUser>,
        GT extends SCIMGroup<? extends SCIMBaseMeta>, P extends SCIMBasePatch>
        implements SCIMService<UT, GT, P> {

    protected static final Log LOG = Log.getLog(AbstractSCIMService.class);

    protected final SCIMConnectorConfiguration config;

    public static final String RESPONSE_ERRORS = "Errors";

    public static final String RESPONSE_RESOURCES = "Resources";

    public AbstractSCIMService(final SCIMConnectorConfiguration config) {
        this.config = config;
    }

    protected WebClient getWebclient(final String path, final Map<String, String> params) {
        WebClient webClient;
        if (StringUtil.isNotBlank(config.getBearerToken()) || (StringUtil.isNotBlank(config.getCliendId())
                && StringUtil.isNotBlank(config.getClientSecret()) && StringUtil.isNotBlank(
                config.getAccessTokenBaseAddress()) && StringUtil.isNotBlank(config.getAccessTokenNodeId()))) {
            webClient =
                    WebClient.create(config.getBaseAddress()).type(config.getAccept()).accept(config.getContentType())
                            .path(path);
            webClient.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken());
        } else {
            webClient = WebClient.create(config.getBaseAddress(), config.getUsername(),
                            config.getPassword() == null ? null : SecurityUtil.decrypt(config.getPassword()), null)
                    .type(config.getAccept()).accept(config.getContentType()).path(path);
        }

        // set content-type and accept headers
        webClient.header(HttpHeaders.ACCEPT, "application/scim+json");
        webClient.header(HttpHeaders.CONTENT_TYPE, "application/scim+json");

        if (params != null) {
            for (Entry<String, String> entry : params.entrySet()) {
                webClient.query(entry.getKey(), entry.getValue()); // will encode parameter
            }
        }

        return webClient;
    }

    private String generateToken() {
        if (StringUtil.isNotBlank(config.getBearerToken())) {
            return config.getBearerToken();
        }
        WebClient webClient =
                WebClient.create(config.getAccessTokenBaseAddress()).type(config.getAccessTokenContentType())
                        .accept(config.getAccept());

        String contentUri = new StringBuilder("&client_id=").append(config.getCliendId()).append("&client_secret=")
                .append(config.getClientSecret()).append("&username=").append(config.getUsername()).append("&password=")
                .append(SecurityUtil.decrypt(config.getPassword())).toString();
        String token = null;
        try {
            Response response = webClient.post(contentUri);
            String responseAsString = response.readEntity(String.class);
            JsonNode result = SCIMUtils.MAPPER.readTree(responseAsString);
            if (result == null || !result.hasNonNull(config.getAccessTokenNodeId())) {
                SCIMUtils.handleGeneralError("No access token found - " + responseAsString);
            }
            token = result.get(config.getAccessTokenNodeId()).textValue();
        } catch (Exception ex) {
            SCIMUtils.handleGeneralError("While obtaining authentication token", ex);
        }

        return token;
    }

    protected JsonNode doGet(final WebClient webClient) {
        LOG.ok("GET: {0}", webClient.getCurrentURI());
        JsonNode result = null;

        try {
            Response response = webClient.get();
            String responseAsString = response.readEntity(String.class);
            checkServiceErrors(response);
            result = SCIMUtils.MAPPER.readTree(responseAsString);
            if (result == null) {
                LOG.ok("Empty result from GET request");
                result = SCIMUtils.MAPPER.createObjectNode();
            }
            if (result.isArray() && (!result.has(RESPONSE_RESOURCES) || result.get(RESPONSE_RESOURCES).isNull())) {
                SCIMUtils.handleGeneralError("Wrong response from GET request: " + responseAsString);
            }
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error(ex, "While retrieving data from SCIM API");
        }

        return result;
    }

    protected void doCreate(final UT user, final WebClient webClient) {
        LOG.ok("CREATE: {0}", webClient.getCurrentURI());
        Response response;
        String payload = null;

        try {
            // check custom attributes
            JsonNode customAttributesNode = buildCustomAttributesNode(config.getCustomAttributesJSON(), user);
            if (customAttributesNode != null) {
                // add custom attributes to payload
                JsonNode userNode = null;
                try {
                    userNode = mergeNodes(SCIMUtils.MAPPER.readTree(SCIMUtils.MAPPER.writeValueAsString(user)),
                            customAttributesNode);
                } catch (JsonProcessingException ex) {
                    SCIMUtils.handleGeneralError("While converting user to node", ex);
                }
                payload = SCIMUtils.MAPPER.writeValueAsString(userNode);
            } else {
                // no custom attributes
                payload = SCIMUtils.MAPPER.writeValueAsString(user);
            }
            response = webClient.post(payload);

            checkServiceErrors(response);
            String value = SCIMAttributeUtils.ATTRIBUTE_ID;
            String responseAsString = response.readEntity(String.class);
            JsonNode responseObj = SCIMUtils.MAPPER.readTree(responseAsString);
            if (responseObj.hasNonNull(value)) {
                user.setId(responseObj.get(value).textValue());
            } else {
                LOG.error("CREATE payload {0}: ", payload);
                SCIMUtils.handleGeneralError(
                        "While getting " + value + " value for created User - Response : " + responseAsString);
            }
        } catch (IOException ex) {
            LOG.error("CREATE payload {0}: ", payload);
            SCIMUtils.handleGeneralError("While creating User", ex);
        }
    }

    protected JsonNode doUpdate(final UT user, final WebClient webClient) {
        LOG.ok("UPDATE: {0}", webClient.getCurrentURI());
        JsonNode result = null;
        Response response;
        String payload = null;
        if (config.getUpdateMethod().equalsIgnoreCase("PATCH")) {
            WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);
        }

        try {
            // check custom attributes
            JsonNode customAttributesNode = buildCustomAttributesNode(config.getCustomAttributesJSON(), user);
            if (customAttributesNode != null) {
                // add custom attributes to payload
                JsonNode userNode = null;
                try {
                    userNode = mergeNodes(SCIMUtils.MAPPER.readTree(SCIMUtils.MAPPER.writeValueAsString(user)),
                            customAttributesNode);
                } catch (JsonProcessingException ex) {
                    SCIMUtils.handleGeneralError("While converting user to node", ex);
                }
                payload = SCIMUtils.MAPPER.writeValueAsString(userNode);
            } else {
                // no custom attributes
                payload = SCIMUtils.MAPPER.writeValueAsString(user);
            }

            if (config.getUpdateMethod().equalsIgnoreCase("PATCH")) {
                response = webClient.invoke("PATCH", payload);
            } else {
                response = webClient.put(payload);
            }

            checkServiceErrors(response);
            result = SCIMUtils.MAPPER.readTree(response.readEntity(String.class));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error("UPDATE payload {0}: ", payload);
            SCIMUtils.handleGeneralError("While updating User", ex);
        }

        return result;
    }

    protected JsonNode doUpdatePatch(final Set<Attribute> replaceAttributes, final WebClient webClient) {
        LOG.ok("UPDATE PATCH: {0}", webClient.getCurrentURI());
        // TODO
        return null;
    }

    protected JsonNode doUpdatePatch(final P patch, final Set<Attribute> replaceAttributes, final WebClient webClient) {
        LOG.ok("UPDATE PATCH: {0}", webClient.getCurrentURI());
        JsonNode result = null;
        Response response;
        String payload = null;
        WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);

        try {
            // no custom attributes
            payload =
                    SCIMUtils.MAPPER.writeValueAsString(patch == null ? buildPatchFromAttrs(replaceAttributes) : patch);

            response = webClient.invoke("PATCH", payload);

            checkServiceErrors(response);
            result = SCIMUtils.MAPPER.readTree(response.readEntity(String.class));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error("UPDATE PATCH payload {0}: ", payload);
            SCIMUtils.handleGeneralError("While updating Group with patch", ex);
        }

        return result;
    }

    protected abstract P buildPatchFromAttrs(Set<Attribute> replaceAttributes);

    protected void doDeleteUser(final String userId, final WebClient webClient) {
        LOG.ok("DELETE: {0}", webClient.getCurrentURI());
        int status = webClient.delete().getStatus();
        if (status != Status.NO_CONTENT.getStatusCode() && status != Status.OK.getStatusCode()) {
            throw new NoSuchEntityException(userId);
        }
    }

    protected void doActivate(final String userId, final WebClient webClient) {
        LOG.ok("ACTIVATE: {0}", webClient.getCurrentURI());
        Response response;
        try {
            ObjectNode userIdNode = SCIMUtils.MAPPER.createObjectNode();
            userIdNode.set("user_id", userIdNode.textNode(userId));

            response = webClient.post(SCIMUtils.MAPPER.writeValueAsString(userIdNode));
            if (response == null) {
                SCIMUtils.handleGeneralError("While activating User - no response");
            } else {
                String responseAsString = response.readEntity(String.class);
                LOG.ok("Response after activating user: {0}", responseAsString);
            }
        } catch (IOException ex) {
            SCIMUtils.handleGeneralError("While activating User", ex);
        }
    }

    private void checkServiceErrors(final Response response) {
        if (response == null) {
            SCIMUtils.handleGeneralError("While executing request - no response");
        }

        String responseAsString = response.readEntity(String.class);
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            throw new NoSuchEntityException(responseAsString);
        } else if (response.getStatus() != Status.OK.getStatusCode()
                && response.getStatus() != Status.ACCEPTED.getStatusCode()
                && response.getStatus() != Status.CREATED.getStatusCode()) {
            SCIMUtils.handleGeneralError("While executing request: " + responseAsString);
        }
    }

    private void checkServiceResultErrors(final JsonNode node, final Response response) {
        if (node.has(RESPONSE_ERRORS)) {
            SCIMUtils.handleGeneralError(response.readEntity(String.class));
        }
    }

    private JsonNode buildCustomAttributesNode(final String customAttributesJSON, final UT user) {
        JsonNode rootNode = null;
        if (StringUtil.isNotBlank(customAttributesJSON) && !user.getSCIMCustomAttributes().isEmpty()) {
            rootNode = SCIMUtils.MAPPER.createObjectNode();
            for (SCIMBaseAttribute<? extends SCIMBaseAttribute> scimAttribute : user.getSCIMCustomAttributes()
                    .keySet()) {
                if (scimAttribute.getType().equals(SCIMAttributeUtils.SCIM_SCHEMA_TYPE_COMPLEX)) {
                    for (SCIMBaseAttribute<? extends SCIMBaseAttribute> scimSubAttribute 
                            : scimAttribute.getSubAttributes()) {
                        buildCustomSimpleAttributeNode(rootNode, scimSubAttribute, user);
                    }
                } else {
                    buildCustomSimpleAttributeNode(rootNode, scimAttribute, user);
                }
            }
        }

        return rootNode;
    }

    private void buildCustomSimpleAttributeNode(final JsonNode rootNode,
            final SCIMBaseAttribute<? extends SCIMBaseAttribute> scimAttribute, final UT user) {
        ObjectNode newNode = SCIMUtils.MAPPER.createObjectNode();
        List<Object> values = user.getSCIMCustomAttributes().get(scimAttribute);
        Object value = null;

        if (!scimAttribute.getMultiValued()) {
            value = values.get(0);
        }
        String mainNodeKey = scimAttribute instanceof SCIMv2Attribute ? SCIMv2Attribute.class.cast(scimAttribute)
                .getExtensionSchema() : SCIMv11Attribute.class.cast(scimAttribute).getSchema();
        String currentNodeKey = scimAttribute.getName();

        if (scimAttribute.getType().equals(SCIMAttributeUtils.SCIM_SCHEMA_TYPE_COMPLEX)) {
            LOG.warn("Too many 'complex' type custom attributes, while parsing custom attribute {0} with schema {1}",
                    currentNodeKey, mainNodeKey);
        } else {
            if (mainNodeKey.contains(SCIMAttributeUtils.SCIM_SCHEMA_EXTENSION)) {
                if (rootNode.has(mainNodeKey)) {
                    ((ObjectNode) rootNode.get(mainNodeKey)).putPOJO(currentNodeKey,
                            values.size() > 1 ? values : values.get(0));
                } else {
                    newNode.putPOJO(currentNodeKey, value == null ? values : value);
                    ((ObjectNode) rootNode).set(mainNodeKey, newNode);
                }
            } else {
                ((ObjectNode) rootNode).putPOJO(currentNodeKey, value == null ? values : value);
            }
        }
    }

    private JsonNode mergeNodes(final JsonNode mainNode, final JsonNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
            JsonNode updatedValue = updateNode.get(updatedFieldName);

            // If the node is an @ArrayNode
            if (valueToBeUpdated != null && valueToBeUpdated.isArray() && updatedValue.isArray()) {
                // running a loop for all elements of the updated ArrayNode
                for (int i = 0; i < updatedValue.size(); i++) {
                    JsonNode updatedChildNode = updatedValue.get(i);
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    if (valueToBeUpdated.size() <= i) {
                        ((ArrayNode) valueToBeUpdated).add(updatedChildNode);
                    }
                    // getting reference for the node to be updated
                    JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
                    mergeNodes(childNodeToBeUpdated, updatedChildNode);
                }
                // if the Node is an @ObjectNode
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
                mergeNodes(valueToBeUpdated, updatedValue);
            } else {
                if (mainNode instanceof ObjectNode) {
                    ((ObjectNode) mainNode).replace(updatedFieldName, updatedValue);
                }
            }
        }

        return mainNode;
    }

    public static <T extends SCIMBaseAttribute<T>> SCIMSchema<T> extractSCIMSchemas(final String json,
            final Class<T> attrType) {
        try {
            SCIMSchema<T> scimSchema = SCIMUtils.MAPPER.readValue(json,
                    SCIMUtils.MAPPER.getTypeFactory().constructParametricType(SCIMSchema.class, attrType));
            // if SCIMv2Attribute populate transient field extensionSchema of the attribute since from SCIM 2.0 "schema"
            // attribute has been removed
            // refer to https://datatracker.ietf.org/doc/html/rfc7643#section-8.7.1
            if (SCIMv2Attribute.class.equals(attrType)) {
                scimSchema.getAttributes()
                        .forEach(attr -> SCIMv2Attribute.class.cast(attr).setExtensionSchema(scimSchema.getId()));
            }
            return scimSchema;
        } catch (IOException ex) {
            LOG.error(ex, "While parsing custom attributes JSON object, taken from connector configuration");
        }
        return null;
    }

    protected <T extends SCIMBaseAttribute<T>> void readCustomAttributes(final UT user, final JsonNode node,
            final Class<T> attrType) {
        if (StringUtil.isNotBlank(config.getCustomAttributesJSON())) {
            SCIMSchema<T> scimSchema = extractSCIMSchemas(config.getCustomAttributesJSON(), attrType);

            if (scimSchema != null && !scimSchema.getAttributes().isEmpty()) {
                for (T attribute : scimSchema.getAttributes()) {
                    List<JsonNode> foundWithSchemaAsKey = node.findValues(
                            attribute instanceof SCIMv2Attribute ? SCIMv2Attribute.class.cast(attribute)
                                    .getExtensionSchema() : SCIMv11Attribute.class.cast(attribute).getSchema());
                    if (!foundWithSchemaAsKey.isEmpty()) {
                        List<Object> values = new ArrayList<>();
                        // manage multiple types
                        values.add(Type.integer.name().equals(attribute.getType()) ? foundWithSchemaAsKey.get(0)
                                .get(attribute.getName()).intValue()
                                : Type.BOOLEAN.name().toLowerCase().equals(attribute.getType())
                                        ? foundWithSchemaAsKey.get(0).get(attribute.getName()).booleanValue()
                                        : foundWithSchemaAsKey.get(0).get(attribute.getName()).textValue());
                        user.getReturnedCustomAttributes()
                                .put((attribute instanceof SCIMv2Attribute ? SCIMv2Attribute.class.cast(attribute)
                                        .getExtensionSchema() : SCIMv11Attribute.class.cast(attribute).getSchema())
                                        + "." + attribute.getName(), values);
                    }
                }
            }
        }
    }

    protected void readCustomAttributes(final PagedResults<UT> resources, final JsonNode node) {
        for (UT resource : resources.getResources()) {
            readCustomAttributes(resource, node, SCIMv2Attribute.class);
        }
    }

    /**
     * @param userId
     */
    @Override public void deleteUser(final String userId) {
        doDeleteUser(userId, getWebclient("Users", null).path(userId));
    }

    /**
     * @param userId
     */
    @Override public void activateUser(final String userId) {
        doActivateUser(userId);
    }

    protected <T extends SCIMBaseAttribute<T>> UT doGetUser(final WebClient webClient, final Class<UT> userType,
            final Class<T> attrType) {
        UT user = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving User from service");
        }

        try {
            user = SCIMUtils.MAPPER.readValue(node.toString(), userType);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to User");
        }

        if (user == null) {
            SCIMUtils.handleGeneralError("While retrieving user from service after create");
        } else {
            // check custom attributes
            readCustomAttributes(user, node, attrType);
        }

        return user;
    }

    protected void doActivateUser(final String userId) {
        doActivate(userId, getWebclient("activation", null).path("tokens"));
    }

    protected UT doCreateUser(final UT user) {
        doCreate(user, getWebclient("Users", null));
        return user;
    }

    protected UT doUpdateUser(final UT user, final Set<Attribute> replaceAttributes, final Class<UT> userType) {
        if (StringUtil.isBlank(user.getId())) {
            SCIMUtils.handleGeneralError("Missing required user id attribute for update");
        }

        UT updated = null;
        JsonNode node =
                config.getUpdateMethod().equalsIgnoreCase("PATCH") && !replaceAttributes.isEmpty() ? doUpdatePatch(
                        replaceAttributes, getWebclient("Users", null).path(user.getId()))
                        : doUpdate(user, getWebclient("Users", null).path(user.getId()));
        if (node == null) {
            SCIMUtils.handleGeneralError("While running update on service");
        }

        try {
            updated = SCIMUtils.MAPPER.readValue(node.toString(), userType);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to User");
        }

        if (updated == null) {
            SCIMUtils.handleGeneralError("While retrieving user from service after update");
        }

        return updated;
    }

    @Override public boolean testService() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        return getAllUsers(1, 1, attributesToGet) != null;
    }

    /**
     * @param attributesToGet
     * @return List of Users
     */
    @Override public List<UT> getAllUsers(final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(attributesToGet, config.getCustomAttributesJSON(),
                    SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient).getResources();
    }

    /**
     * @param filterQuery     to filter results
     * @param attributesToGet
     * @return Filtered list of Users
     */
    @Override public List<UT> getAllUsers(final String filterQuery, final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        params.put("filter", filterQuery);
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(attributesToGet, config.getCustomAttributesJSON(),
                    SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient).getResources();
    }

    /**
     * @param startIndex
     * @param count
     * @param attributesToGet
     * @return Paged list of Users
     */
    @Override public PagedResults<UT> getAllUsers(final Integer startIndex, final Integer count,
            final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(attributesToGet, config.getCustomAttributesJSON(),
                    SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient);
    }

    /**
     * @param filterQuery
     * @param startIndex
     * @param count
     * @param attributesToGet
     * @return Paged and Filtered list of Users
     */
    public PagedResults<UT> getAllUsers(final String filterQuery, final Integer startIndex, final Integer count,
            final Set<String> attributesToGet) {

        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        params.put("filter", filterQuery);
        params.put("attributes", SCIMUtils.cleanAttributesToGet(attributesToGet, config.getCustomAttributesJSON(),
                SCIMv2Attribute.class));
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient);
    }

    protected PagedResults<UT> doGetAllUsers(final WebClient webClient) {
        PagedResults<UT> pagedResults = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Users from service");
        }

        try {
            pagedResults = deserializeUserPagedResults(node.toString());
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Users");
        }

        if (pagedResults == null) {
            SCIMUtils.handleGeneralError("While retrieving Users from service");
        } else {
            // check custom attributes
            if (!pagedResults.getResources().isEmpty()) {
                readCustomAttributes(pagedResults, node.get(RESPONSE_RESOURCES));
            }
        }

        return pagedResults;
    }

    protected GT doGetGroup(final WebClient webClient, final Class<GT> userType) {
        GT group = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Group from service");
        }

        try {
            group = SCIMUtils.MAPPER.readValue(node.toString(), userType);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Group");
        }

        if (group == null) {
            SCIMUtils.handleGeneralError("While retrieving group from service");
        }

        return group;
    }

    protected GT doCreateGroup(final GT group) {
        doCreate(group, getWebclient("Groups", null));
        return group;
    }

    @Override public PagedResults<GT> getAllGroups(final Integer startIndex, final Integer count) {
        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        WebClient webClient = getWebclient("Groups", params);
        return doGetAllGroups(webClient);
    }

    @Override public List<GT> getAllGroups() {
        WebClient webClient = getWebclient("Groups", Collections.emptyMap());
        return doGetAllGroups(webClient).getResources();
    }

    @Override public List<GT> getAllGroups(final String filterQuery) {
        Map<String, String> params = new HashMap<>();
        params.put("filter", filterQuery);
        WebClient webClient = getWebclient("Groups", params);
        return doGetAllGroups(webClient).getResources();
    }

    public PagedResults<GT> getAllGroups(final String filterQuery, final Integer startIndex, final Integer count) {

        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        params.put("filter", filterQuery);
        WebClient webClient = getWebclient("Users", params);
        return doGetAllGroups(webClient);
    }

    protected PagedResults<GT> doGetAllGroups(final WebClient webClient) {
        PagedResults<GT> pagedResults = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Groups from service");
        }

        try {
            pagedResults = deserializeGroupPagedResults(node.toString());
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Users");
        }

        if (pagedResults == null) {
            SCIMUtils.handleGeneralError("While retrieving Groups from service");
        }

        return pagedResults;
    }

    @Override public void deleteGroup(final String groupId) {

    }

    @Override public GT createGroup(final GT group) {
        return doCreateGroup(group);
    }

    protected void doCreate(final GT group, final WebClient webClient) {
        LOG.ok("CREATE: {0}", webClient.getCurrentURI());
        Response response;
        String payload = null;

        try {
            payload = SCIMUtils.MAPPER.writeValueAsString(group);
            response = webClient.post(payload);

            checkServiceErrors(response);
            String value = SCIMAttributeUtils.ATTRIBUTE_ID;
            String responseAsString = response.readEntity(String.class);
            JsonNode responseObj = SCIMUtils.MAPPER.readTree(responseAsString);
            if (responseObj.hasNonNull(value)) {
                group.setId(responseObj.get(value).textValue());
            } else {
                LOG.error("CREATE payload {0}: ", payload);
                SCIMUtils.handleGeneralError(
                        "While getting " + value + " value for created Group - Response : " + responseAsString);
            }
        } catch (IOException ex) {
            LOG.error("CREATE payload {0}: ", payload);
            SCIMUtils.handleGeneralError("While creating Group", ex);
        }
    }

    protected GT doUpdateGroup(final GT group, final P patch, final Class<GT> groupType) {
        return doUpdateGroup(group, Collections.emptySet(), patch, groupType);
    }

    protected GT doUpdateGroup(final GT group, final Set<Attribute> replaceAttributes, final P patch,
            final Class<GT> groupType) {
        if (StringUtil.isBlank(group.getId())) {
            SCIMUtils.handleGeneralError("Missing required group id attribute for update");
        }

        GT updated = null;
        JsonNode node =
                config.getUpdateGroupMethod().equalsIgnoreCase("PATCH") ? doUpdatePatch(patch, replaceAttributes,
                        getWebclient("Groups", null).path(group.getId()))
                        : doUpdate(group, getWebclient("Groups", null).path(group.getId()));
        if (node == null) {
            SCIMUtils.handleGeneralError("While running update group on service");
        }

        try {
            updated = SCIMUtils.MAPPER.readValue(node.toString(), groupType);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Group");
        }

        if (updated == null) {
            SCIMUtils.handleGeneralError("While retrieving group from service after update");
        }

        return updated;
    }

    protected JsonNode doUpdate(final GT group, final WebClient webClient) {
        LOG.ok("UPDATE: {0}", webClient.getCurrentURI());
        JsonNode result = null;
        Response response;
        String payload = null;
        if (config.getUpdateMethod().equalsIgnoreCase("PATCH")) {
            WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);
        }

        try {
            // no custom attributes
            payload = SCIMUtils.MAPPER.writeValueAsString(group);

            if (config.getUpdateMethod().equalsIgnoreCase("PATCH")) {
                response = webClient.invoke("PATCH", payload);
            } else {
                response = webClient.put(payload);
            }

            checkServiceErrors(response);
            result = SCIMUtils.MAPPER.readTree(response.readEntity(String.class));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error("UPDATE payload {0}: ", payload);
            SCIMUtils.handleGeneralError("While updating Group", ex);
        }

        return result;
    }

    protected void doDeleteGroup(final String groupId, final WebClient webClient) {
        LOG.ok("DELETE Group: {0}", webClient.getCurrentURI());
        int status = webClient.delete().getStatus();
        if (status != Status.NO_CONTENT.getStatusCode() && status != Status.OK.getStatusCode()) {
            throw new NoSuchEntityException(groupId);
        }
    }

    protected abstract PagedResults<UT> deserializeUserPagedResults(String node) throws JsonProcessingException;
    protected abstract PagedResults<GT> deserializeGroupPagedResults(String node) throws JsonProcessingException;

}
