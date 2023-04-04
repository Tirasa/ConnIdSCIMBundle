/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
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
package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv11EnterpriseUser implements SCIMEnterpriseUser<SCIMv11Manager> {

    @JsonProperty("employeeNumber")
    private String employeeNumber;

    @JsonProperty("manager")
    private SCIMv11Manager manager;

    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public void setEmployeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    @Override
    public SCIMv11Manager getManager() {
        return manager;
    }

    @Override
    public void setManager(final SCIMv11Manager manager) {
        this.manager = manager;
    }

    @Override
    public Set<Attribute> toAttributes(final String id) throws IllegalArgumentException, IllegalAccessException {
        // TODO
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("employeeNumber", employeeNumber)
                .append("manager", manager)
                .toString();
    }
}
