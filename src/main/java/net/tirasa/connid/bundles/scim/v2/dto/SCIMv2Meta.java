/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.tirasa.connid.bundles.scim.v2.dto;

import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SCIMv2Meta extends SCIMBaseMeta {

    private static final long serialVersionUID = -9162917034280030708L;

    private String resourceType;

    public SCIMv2Meta() {
    }

    public SCIMv2Meta(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("resourceType", resourceType)
                .append("created", created)
                .append("lastModified", lastModified)
                .append("location", location)
                .append("version", version)
                .toString();
    }
}
