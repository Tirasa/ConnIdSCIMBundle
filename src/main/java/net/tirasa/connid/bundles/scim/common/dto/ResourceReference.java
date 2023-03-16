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

package net.tirasa.connid.bundles.scim.common.dto;

import java.io.Serializable;

public class ResourceReference implements Serializable {

    private static final long serialVersionUID = 9126588075353486789L;

    public enum ReferenceType {
        direct, indirect, User, Group;
    }

    private String value;

    private String ref;

    private String display;

    private ReferenceType type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("value", value)
                .append("ref", ref)
                .append("display", display)
                .append("type", type)
                .toString();
    }
}
