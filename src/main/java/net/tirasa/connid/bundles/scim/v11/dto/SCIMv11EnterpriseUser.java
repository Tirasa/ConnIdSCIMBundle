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
package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

public class SCIMv11EnterpriseUser implements SCIMEnterpriseUser<SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager> {

    private static final long serialVersionUID = -8540303884204701777L;

    public static class SCIMv11EnterpriseUserManager implements Serializable {

        private static final long serialVersionUID = -7930518578899296192L;

        @JsonProperty("managerId")
        private String managerId;

        @JsonProperty("displayName")
        private String displayName;

        public String getManagerId() {
            return managerId;
        }

        public void setManagerId(final String managerId) {
            this.managerId = managerId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager value(final String value) {
            this.managerId = value;
            return this;
        }

        public SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public List<Attribute> toAttributes() {
            return Arrays.asList(
                    AttributeBuilder.build(SCHEMA_URI + ":manager.managerId", this.managerId),
                    AttributeBuilder.build(SCHEMA_URI + ":manager.displayName", this.displayName));
        }

        @Override
        public String toString() {
            return "SCIMv11EnterpriseUserManager{"
                    + "managerId=" + managerId
                    + ", displayName=" + displayName + '}';
        }
    }

    public static final String SCHEMA_URI = "urn:scim:schemas:extension:enterprise:1.0";

    @JsonProperty("employeeNumber")
    private String employeeNumber;

    @JsonProperty("manager")
    private SCIMv11EnterpriseUserManager manager;

    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public void setEmployeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    @Override
    public SCIMv11EnterpriseUserManager getManager() {
        return manager;
    }

    @Override
    public void setManager(final SCIMv11EnterpriseUserManager manager) {
        this.manager = manager;
    }

    @Override
    public Set<Attribute> toAttributes(final String schemaUri, final boolean useColon)
            throws IllegalArgumentException, IllegalAccessException {

        Set<Attribute> attrs = new HashSet<>();

        for (Field field : SCIMUtils.getAllFieldsList(getClass()).stream().
                filter(f -> !"SCHEMA_URI".equals(f.getName()) && !"serialVersionUID".equals(f.getName())).
                collect(Collectors.toList())) {

            if (SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager.class.equals(field.getType()) && manager != null) {
                attrs.addAll(manager.toAttributes());
            } else if (!field.isAnnotationPresent(JsonIgnore.class)) {
                field.setAccessible(true);
                // simple attribute can have the colon as separator
                attrs.add(AttributeBuilder.build(
                        schemaUri + (useColon ? ":" : ".") + field.getName(), field.get(this)));
            }
        }

        return attrs;
    }

    @Override
    public String toString() {
        return "SCIMv11EnterpriseUser{"
                + "employeeNumber=" + employeeNumber
                + ", manager=" + manager + '}';
    }
}
