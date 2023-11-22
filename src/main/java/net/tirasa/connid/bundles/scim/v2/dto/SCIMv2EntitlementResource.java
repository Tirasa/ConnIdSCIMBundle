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
package net.tirasa.connid.bundles.scim.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEntitlement;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv2EntitlementResource extends AbstractSCIMBaseResource<SCIMv2Meta>
        implements SCIMEntitlement<SCIMv2Meta> {

    private static final long serialVersionUID = 328618763364322821L;

    public static final String SCHEMA_URI = "urn:salesforce:schemas:extension:1.0:Entitlement";

    public static final String RESOURCE_NAME = "Entitlements";

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String type;

    private final List<BaseResourceReference> members = new ArrayList<>();

    public SCIMv2EntitlementResource() {
    }

    protected SCIMv2EntitlementResource(final String schemaUri, final SCIMv2Meta meta) {
        super(meta);
        this.baseSchema = schemaUri;
        schemas.add(baseSchema);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public List<BaseResourceReference> getMembers() {
        return members;
    }

    @Override
    public Set<Attribute> toAttributes(final Class<?> type, final SCIMConnectorConfiguration configuration)
            throws IllegalArgumentException, IllegalAccessException {
        return Collections.emptySet();
    }

    @Override
    public void fromAttributes(final Set<Attribute> attributes, final boolean replaceMembersOnUpdate) {
        // DO NOTHING
    }
}
