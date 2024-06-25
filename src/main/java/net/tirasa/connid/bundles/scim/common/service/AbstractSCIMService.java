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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.PagedResults;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBasePatch;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.Type;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public abstract class AbstractSCIMService<UT extends SCIMUser<
        ? extends SCIMBaseMeta, ? extends SCIMEnterpriseUser<?>>, 
        GT extends SCIMGroup<? extends SCIMBaseMeta>,
        ERT extends SCIMBaseResource<? extends SCIMBaseMeta>,
        P extends SCIMBasePatch>
        implements SCIMService<UT, GT, ERT, P> {

    protected static final Log LOG = Log.getLog(AbstractSCIMService.class);

    protected final SCIMConnectorConfiguration config;

    public static final String RESPONSE_ERRORS = "Errors";

    public static final String RESPONSE_RESOURCES = "Resources";

    public static final int MAX_RETRIES = 3;

    public AbstractSCIMService(final SCIMConnectorConfiguration config) {
        this.config = config;
    }

    protected WebClient getWebclient(final String path, final Map<String, String> params) {
        WebClient webClient;
        if (checkBearerToken()) {

            webClient = WebClient.create(config.getBaseAddress()).
                    header(HttpHeaders.AUTHORIZATION, "Bearer " + getBearerToken(false));
        } else {
            webClient = WebClient.create(
                    config.getBaseAddress(),
                    config.getUsername(),
                    config.getPassword() == null ? null : SecurityUtil.decrypt(config.getPassword()),
                    null);
        }

        if (StringUtil.isNotBlank(config.getProxyServerHost())) {
            HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();

            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setProxyServer(config.getProxyServerHost());
            policy.setProxyServerPort(config.getProxyServerPort());
            policy.setProxyServerType(ProxyServerType.valueOf(config.getProxyServerType().toUpperCase()));
            conduit.setClient(policy);

            if (StringUtil.isNotBlank(config.getProxyServerUser())
                    && StringUtil.isNotBlank(config.getProxyServerPassword())) {
                ProxyAuthorizationPolicy authorizationPolicy = new ProxyAuthorizationPolicy();
                authorizationPolicy.setAuthorizationType("Basic");
                authorizationPolicy.setUserName(config.getProxyServerUser());
                authorizationPolicy.setPassword(config.getProxyServerPassword());
                conduit.setProxyAuthorization(authorizationPolicy);
            }
        }

        if (config.getFollowHttpRedirects()) {
            HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();
            final HTTPClientPolicy policy = conduit.getClient();
            policy.setAutoRedirect(true);
            conduit.setClient(policy);
        }
        
        webClient.type(config.getContentType()).accept(config.getAccept()).path(path);

        Optional.ofNullable(params).ifPresent(p -> p.forEach((k, v) -> webClient.query(k, v)));

        return webClient;
    }

    protected String getBearerToken(final boolean forceRefresh) {
        if (StringUtil.isNotBlank(config.getBearerToken()) && !forceRefresh) {
            return config.getBearerToken();
        }

        WebClient webClient = WebClient.create(config.getAccessTokenBaseAddress()).
                type(config.getAccessTokenContentType()).accept(config.getAccessTokenAccept());

        StringBuilder requestBuilder =
                new StringBuilder("&client_id=").append(config.getClientId())
                        .append("&client_secret=").append(config.getClientSecret());
        // append also username and password if and only if they're not blank
        if (StringUtil.isNotBlank(config.getUsername()) && config.getPassword() != null) {
            requestBuilder.append("&username=").append(config.getUsername())
                    .append("&password=").append(SecurityUtil.decrypt(config.getPassword())).toString();
        }

        String token = null;
        try {
            Response response = webClient.post(requestBuilder.toString());
            String body = response.readEntity(String.class);
            JsonNode result = SCIMUtils.MAPPER.readTree(body);
            if (result == null || !result.hasNonNull(config.getAccessTokenNodeId())) {
                SCIMUtils.handleGeneralError("No access token found - " + body);
            }
            token = result.get(config.getAccessTokenNodeId()).textValue();
            config.setBearerToken(token);
        } catch (Exception ex) {
            SCIMUtils.handleGeneralError("While obtaining authentication token", ex);
        }

        return token;
    }

    protected JsonNode doGet(final WebClient webClient) {
        LOG.ok("GET: {0}", webClient.getCurrentURI());

        JsonNode result = null;
        try {
            Response response = executeAndRetry(WebClient::get, webClient, 0);
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

        try {
            String payload;

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
            LOG.ok("CREATE payload is {0}: ", payload);
            Response response = executeAndRetry(wc -> wc.post(payload), webClient, 0);
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
            LOG.error(ex, "Error while creating entity");
            SCIMUtils.handleGeneralError("While creating User", ex);
        }
    }

    protected JsonNode doUpdate(final UT user, final WebClient webClient) {
        LOG.ok("UPDATE: {0}", webClient.getCurrentURI());

        if (config.getUpdateUserMethod().equalsIgnoreCase("PATCH")) {
            WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);
        }

        JsonNode result = null;
        try {
            String payload;

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

            LOG.ok("UPDATE payload is {0}: ", payload);

            Response response;
            if (config.getUpdateUserMethod().equalsIgnoreCase("PATCH")) {
                response = executeAndRetry(wc -> wc.invoke("PATCH", payload), webClient, 0);
            } else {
                response = executeAndRetry(wc -> wc.put(payload), webClient, 0);
            }

            checkServiceErrors(response);
            result = SCIMUtils.MAPPER.readTree(response.readEntity(String.class));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error(ex, "Error while updating entity");
            SCIMUtils.handleGeneralError("While updating User", ex);
        }

        return result;
    }

    protected JsonNode doUpdatePatch(final Set<Attribute> replaceAttributes, final WebClient webClient) {
        LOG.ok("UPDATE PATCH: {0}", webClient.getCurrentURI());
        return doUpdatePatch(null, replaceAttributes, webClient);
    }

    protected JsonNode doUpdatePatch(final P patch, final Set<Attribute> replaceAttributes, final WebClient webClient) {
        LOG.ok("UPDATE PATCH: {0}", webClient.getCurrentURI());

        WebClient.getConfig(webClient).getRequestContext().put("use.async.http.conduit", true);

        JsonNode result = null;
        try {
            // no custom attributes
            String payload = SCIMUtils.MAPPER.writeValueAsString(
                    patch == null ? buildPatchFromAttrs(replaceAttributes) : patch);

            LOG.ok("UPDATE PATCH payload is {0}: ", payload);

            Response response = executeAndRetry(wc -> webClient.invoke("PATCH", payload), webClient, 0);
            checkServiceErrors(response);

            // some providers, like AWS, return no result, thus a new read is needed
            result = Status.NO_CONTENT.getStatusCode() == response.getStatus()
                    ? doGet(webClient)
                    : SCIMUtils.MAPPER.readTree(response.readEntity(String.class));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error(ex, "Error while updating entity");
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

        try {
            ObjectNode userIdNode = SCIMUtils.MAPPER.createObjectNode();
            userIdNode.set("user_id", userIdNode.textNode(userId));

            String payload = SCIMUtils.MAPPER.writeValueAsString(userIdNode);

            LOG.ok("Activate payload is {0}", payload);

            Response response = executeAndRetry(wc -> wc.post(payload), webClient, 0);
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

    protected void checkServiceErrors(final Response response) {
        if (response == null) {
            SCIMUtils.handleGeneralError("While executing request - no response");
        }

        String responseAsString = response.readEntity(String.class);
        if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            throw new NoSuchEntityException(responseAsString);
        }
        if (response.getStatusInfo().getFamily() != Status.Family.SUCCESSFUL) {
            SCIMUtils.handleGeneralError("While executing request: " + responseAsString);
        }
    }

    protected void checkServiceResultErrors(final JsonNode node, final Response response) {
        if (node.has(RESPONSE_ERRORS)) {
            SCIMUtils.handleGeneralError(response.readEntity(String.class));
        }
    }

    @SuppressWarnings("rawtypes")
    protected JsonNode buildCustomAttributesNode(final String customAttributesJSON, final UT user) {
        JsonNode rootNode = null;
        if (StringUtil.isNotBlank(customAttributesJSON) && !user.getSCIMCustomAttributes().isEmpty()) {
            rootNode = SCIMUtils.MAPPER.createObjectNode();

            for (SCIMBaseAttribute<?> attr : user.getSCIMCustomAttributes().keySet()) {
                if (attr.getType().equals(SCIMAttributeUtils.SCIM_SCHEMA_TYPE_COMPLEX)) {
                    for (SCIMBaseAttribute<? extends SCIMBaseAttribute> sub : attr.getSubAttributes()) {
                        buildCustomSimpleAttributeNode(rootNode, sub, user);
                    }
                } else {
                    buildCustomSimpleAttributeNode(rootNode, attr, user);
                }
            }
        }

        return rootNode;
    }

    protected void buildCustomSimpleAttributeNode(
            final JsonNode rootNode,
            final SCIMBaseAttribute<? extends SCIMBaseAttribute<?>> scimAttribute,
            final UT user) {

        ObjectNode newNode = SCIMUtils.MAPPER.createObjectNode();
        List<Object> values = user.getSCIMCustomAttributes().get(scimAttribute);

        Object nodeValue = scimAttribute.getMultiValued() ? values : (values.isEmpty() ? null : values.get(0));
        String mainNodeKey = scimAttribute instanceof SCIMv2Attribute ? SCIMv2Attribute.class.cast(scimAttribute)
                .getExtensionSchema() : SCIMv11Attribute.class.cast(scimAttribute).getSchema();
        String currentNodeKey = scimAttribute.getName();

        if (scimAttribute.getType().equals(SCIMAttributeUtils.SCIM_SCHEMA_TYPE_COMPLEX)) {
            LOG.warn("Too many 'complex' type custom attributes, while parsing custom attribute {0} with schema {1}",
                    currentNodeKey, mainNodeKey);
        } else {
            if (mainNodeKey.contains(SCIMAttributeUtils.SCIM_SCHEMA_EXTENSION)) {
                if (rootNode.has(mainNodeKey)) {
                    ((ObjectNode) rootNode.get(mainNodeKey)).putPOJO(currentNodeKey, nodeValue);
                } else {
                    newNode.putPOJO(currentNodeKey, nodeValue);
                    ((ObjectNode) rootNode).set(mainNodeKey, newNode);
                }
            } else {
                ((ObjectNode) rootNode).putPOJO(currentNodeKey, nodeValue);
            }
        }
    }

    protected JsonNode mergeNodes(final JsonNode mainNode, final JsonNode updateNode) {
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

    protected <T extends SCIMBaseAttribute<T>> void readCustomAttributes(
            final UT user, final JsonNode node, final Class<T> attrType) {

        SCIMUtils.extractSCIMSchemas(config.getCustomAttributesJSON(), attrType).ifPresent(scimSchema -> {
            for (T attr : scimSchema.getAttributes()) {
                List<JsonNode> foundWithSchemaAsKey = node.findValues(
                        attr instanceof SCIMv2Attribute
                                ? SCIMv2Attribute.class.cast(attr).getExtensionSchema()
                                : SCIMv11Attribute.class.cast(attr).getSchema());
                if (!foundWithSchemaAsKey.isEmpty() && foundWithSchemaAsKey.get(0).has(attr.getName())) {
                    List<Object> values = new ArrayList<>();

                    // manage multiple types
                    values.add(Type.integer.name().equals(attr.getType())
                            ? foundWithSchemaAsKey.get(0).get(attr.getName()).intValue()
                            : Type.BOOLEAN.name().toLowerCase().equals(attr.getType())
                            ? foundWithSchemaAsKey.get(0).get(attr.getName()).booleanValue()
                            : foundWithSchemaAsKey.get(0).get(attr.getName()).textValue());

                    user.getReturnedCustomAttributes().put(
                            (attr instanceof SCIMv2Attribute
                                    ? SCIMv2Attribute.class.cast(attr).getExtensionSchema()
                                    : SCIMv11Attribute.class.cast(attr).getSchema())
                            + "." + attr.getName(),
                            values);
                }
            }
        });
    }

    protected void readCustomAttributes(final PagedResults<UT> resources, final JsonNode node) {
        for (UT resource : resources.getResources()) {
            readCustomAttributes(resource, node, SCIMv2Attribute.class);
        }
    }

    /**
     * @param userId
     */
    @Override
    public void deleteUser(final String userId) {
        doDeleteUser(userId, getWebclient("Users", null).path(userId));
    }

    /**
     * @param userId
     */
    @Override
    public void activateUser(final String userId) {
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
                config.getUpdateUserMethod().equalsIgnoreCase("PATCH") && !replaceAttributes.isEmpty() ? doUpdatePatch(
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

    @Override
    public boolean testService() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        return getAllUsers(1, 1, attributesToGet) != null;
    }

    /**
     * @param attributesToGet
     * @return List of Users
     */
    @Override
    public List<UT> getAllUsers(final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(attributesToGet, config.getCustomAttributesJSON(),
                    SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient).getResources();
    }

    /**
     * @param filterQuery to filter results
     * @param attributesToGet
     * @return Filtered list of Users
     */
    @Override
    public List<UT> getAllUsers(final String filterQuery, final Set<String> attributesToGet) {
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
    @Override
    public PagedResults<UT> getAllUsers(final Integer startIndex, final Integer count,
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

    protected GT doGetGroup(final WebClient webClient, final Class<GT> groupType) {
        GT group = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Group from service");
        }

        try {
            group = SCIMUtils.MAPPER.readValue(node.toString(), groupType);
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

    @Override
    public PagedResults<GT> getAllGroups(final Integer startIndex, final Integer count) {
        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        WebClient webClient = getWebclient("Groups", params);
        return doGetAllGroups(webClient);
    }

    @Override
    public List<GT> getAllGroups() {
        WebClient webClient = getWebclient("Groups", Collections.emptyMap());
        return doGetAllGroups(webClient).getResources();
    }

    @Override
    public List<GT> getAllGroups(final String filterQuery) {
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
        WebClient webClient = getWebclient("Groups", params);
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

    @Override
    public void deleteGroup(final String groupId) {
        doDeleteGroup(groupId, getWebclient("Groups", null).path(groupId));
    }

    @Override
    public GT createGroup(final GT group) {
        return doCreateGroup(group);
    }

    protected ERT doGetEntitlement(final WebClient webClient, final Class<ERT> entitlementType) {
        ERT entitlement = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Entitlement from service");
        }

        try {
            entitlement = SCIMUtils.MAPPER.readValue(node.toString(), entitlementType);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Entitlement");
        }

        if (entitlement == null) {
            SCIMUtils.handleGeneralError("While retrieving Entitlement from service");
        }

        return entitlement;
    }

    protected void doCreate(final GT group, final WebClient webClient) {
        LOG.ok("CREATE: {0}", webClient.getCurrentURI());

        try {
            String payload = SCIMUtils.MAPPER.writeValueAsString(group);

            LOG.ok("CREATE payload is {0}: ", payload);

            Response response = executeAndRetry(wc -> wc.post(payload), webClient, 0);

            checkServiceErrors(response);
            String value = SCIMAttributeUtils.ATTRIBUTE_ID;
            String responseAsString = response.readEntity(String.class);
            JsonNode responseObj = SCIMUtils.MAPPER.readTree(responseAsString);
            if (responseObj.hasNonNull(value)) {
                group.setId(responseObj.get(value).textValue());
            } else {
                LOG.error("CREATE payload {0} error {1}", payload, responseAsString);
                SCIMUtils.handleGeneralError(
                        "While getting " + value + " value for created Group - Response : " + responseAsString);
            }
        } catch (IOException ex) {
            LOG.error(ex, "Unable to create entity");
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
                config.getUpdateGroupMethod().equalsIgnoreCase("PATCH") && patch != null
                ? doUpdatePatch(patch, replaceAttributes,
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
        String payload;

        try {
            // this is only for update through PUT method
            payload = SCIMUtils.MAPPER.writeValueAsString(group);

            LOG.ok("UPDATE payload is {0}: ", payload);

            response = executeAndRetry(wc -> wc.put(payload), webClient, 0);

            checkServiceErrors(response);
            String responseEntity = response.readEntity(String.class);
            // some servers like Salesforce return empty response on group update with PUT, thus  a re-read is needed
            result = StringUtil.isNotBlank(responseEntity) ? SCIMUtils.MAPPER.readTree(responseEntity)
                    : doGet(getWebclient("Groups", null).path(group.getId()));
            checkServiceResultErrors(result, response);
        } catch (IOException ex) {
            LOG.error(ex, "Unable to update entity");
            SCIMUtils.handleGeneralError("While updating Group", ex);
        }

        return result;
    }

    protected void doDeleteGroup(final String groupId, final WebClient webClient) {
        LOG.ok("DELETE Group: {0}", webClient.getCurrentURI());
        int status = executeAndRetry(WebClient::delete, webClient, 0).getStatus();
        if (status != Status.NO_CONTENT.getStatusCode() && status != Status.OK.getStatusCode()) {
            throw new NoSuchEntityException(groupId);
        }
    }

    protected Response executeAndRetry(
            final Function<WebClient, Response> action,
            final WebClient webClient,
            final int retry) {

        if (checkBearerToken()) {
            Response response = action.apply(
                    webClient.replaceHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearerToken()));
            if (response.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
                if (retry < MAX_RETRIES) {
                    LOG.ok("Refreshing bearer token after UNAUTHORIZED response, try #{0}", retry);
                    // regenerate the token and update configuration
                    getBearerToken(true);
                    return executeAndRetry(action, webClient, retry + 1);
                }

                LOG.error("Max retries {0} reached after unauthorized error", MAX_RETRIES);
            }
            return response;
        } else {
            return action.apply(webClient);
        }
    }

    private boolean checkBearerToken() {
        return StringUtil.isNotBlank(config.getBearerToken())
                || (StringUtil.isNotBlank(config.getClientId())
                && StringUtil.isNotBlank(config.getClientSecret())
                && StringUtil.isNotBlank(config.getAccessTokenBaseAddress())
                && StringUtil.isNotBlank(config.getAccessTokenAccept())
                && StringUtil.isNotBlank(config.getAccessTokenNodeId()));
    }

    protected abstract PagedResults<UT> deserializeUserPagedResults(String node) throws JsonProcessingException;

    protected abstract PagedResults<GT> deserializeGroupPagedResults(String node) throws JsonProcessingException;

}
