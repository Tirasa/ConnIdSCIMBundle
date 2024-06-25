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
package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class AbstractSCIMGroup<MT extends SCIMBaseMeta> extends AbstractSCIMBaseResource<MT> implements SCIMGroup<MT> {

    private static final long serialVersionUID = 328618763364322821L;

    @JsonProperty
    protected String displayName;

    protected final List<BaseResourceReference> members = new ArrayList<>();

    public AbstractSCIMGroup() {
    }

    protected AbstractSCIMGroup(final String schemaUri, final MT meta) {
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
    public List<BaseResourceReference> getMembers() {
        return members;
    }

    @JsonIgnore
    @Override
    @SuppressWarnings("unchecked")
    public Set<Attribute> toAttributes(final Class<?> type, final SCIMConnectorConfiguration configuration)
            throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();

        SCIMUtils.getAllFieldsList(type).stream()
                .filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())
                && !"RESOURCE_NAME".equals(f.getName()) && !"SCHEMA_URI".equals(f.getName())).forEach(field -> {

            try {
                field.setAccessible(true);
                // manage enterprise user
                if (!field.isAnnotationPresent(JsonIgnore.class) && !SCIMUtils.isEmptyObject(field.get(this))) {
                    Object objInstance = field.get(this);

                    if (field.getGenericType().toString().contains(SCIMBaseMeta.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<MT> list = (List<MT>) objInstance;
                            for (MT scimMeta : list) {
                                SCIMAttributeUtils.addAttribute(scimMeta.toAttributes(), attrs,
                                        field.getType());
                            }
                        } else {
                            SCIMAttributeUtils.addAttribute(SCIMBaseMeta.class.cast(objInstance).toAttributes(),
                                    attrs, field.getType());
                        }
                    } else {
                        attrs.add(SCIMAttributeUtils.buildAttributeFromClassField(field, this).build());
                    }
                }
            } catch (IllegalAccessException e) {
                LOG.error(e, "Unable to build user attributes by reflection");
            }
        });

        return attrs;
    }

    @JsonIgnore
    @Override
    public void fromAttributes(final Set<Attribute> attributes, final boolean replaceMembersOnUpdate) {
        attributes.stream().filter(attribute -> !CollectionUtil.isEmpty(attribute.getValue())).forEach(attribute -> {
            try {
                doSetAttribute(attribute.getName(), attribute.getValue(), replaceMembersOnUpdate);
            } catch (Exception e) {
                LOG.warn(e, "While populating User field from ConnId attribute: {0}", attribute);
            }
        });
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    private void doSetAttribute(final String name, final List<Object> values, final boolean replaceMembersOnUpdate) {
        if (SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME.equals(name)) {
            this.displayName = String.class.cast(values.get(0));
        } else if (SCIMAttributeUtils.SCIM_GROUP_MEMBERS.equals(name)) {
            if (replaceMembersOnUpdate) {
                // clear members before, to enable groups replacement
                members.clear();
            }
            values.forEach(value -> members.add(
                    new BaseResourceReference.Builder().value(value.toString()).build()));
        }
    }
}
