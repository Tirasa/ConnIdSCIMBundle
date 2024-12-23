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
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMUserAddress {

    @JsonProperty
    private String formatted;

    @JsonProperty
    private String streetAddress;

    @JsonProperty
    private String locality;

    @JsonProperty
    private String region;

    @JsonProperty
    private String postalCode;

    @JsonProperty
    private String country;

    @JsonProperty
    private AddressCanonicalType type;

    @JsonProperty
    private boolean primary;

    @JsonProperty
    private String operation;

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(final String formatted) {
        this.formatted = formatted;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(final String locality) {
        this.locality = locality;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public AddressCanonicalType getType() {
        return type;
    }

    public void setType(final AddressCanonicalType type) {
        this.type = type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final boolean primary) {
        this.primary = primary;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(final String operation) {
        this.operation = operation;
    }

    public Set<Attribute> toAttributes(final SCIMConnectorConfiguration configuration)
            throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(JsonIgnore.class)) {
                field.setAccessible(true);
                attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(
                        field.get(this),
                        SCIMAttributeUtils.SCIM_USER_ADDRESSES.concat(".")
                                .concat(type == null
                                        ? (StringUtil.isBlank(configuration.getAddressesType())
                                        ? StringUtil.EMPTY
                                        : configuration.getAddressesType().concat("."))
                                        : type.name().concat("."))
                                .concat(field.getName()),
                        field.getType()).build());
            }
        }
        return attrs;
    }

    public boolean isEmpty() {
        return StringUtil.isBlank(this.postalCode)
                && StringUtil.isBlank(this.streetAddress)
                && StringUtil.isBlank(this.locality)
                && StringUtil.isBlank(this.country)
                && StringUtil.isBlank(this.region);
    }

    public SCIMUserAddress fillFrom(final Optional<SCIMUserAddress> currentDefaultAddress) {
        currentDefaultAddress.ifPresent(address -> {
            if (StringUtil.isBlank(this.streetAddress)) {
                setStreetAddress(address.getStreetAddress());
            }
            if (StringUtil.isBlank(this.locality)) {
                setLocality(address.getLocality());
            }
            if (StringUtil.isBlank(this.country)) {
                setCountry(address.getCountry());
            }
            if (StringUtil.isBlank(this.region)) {
                setRegion(address.getRegion());
            }
            if (StringUtil.isBlank(this.postalCode)) {
                setPostalCode(address.getPostalCode());
            }
        });
        return this;
    }

    @Override
    public String toString() {
        return "SCIMUserAddressConf{"
                + "formatted=" + formatted
                + ", streetAddress=" + streetAddress
                + ", locality=" + locality
                + ", region=" + region
                + ", postalCode=" + postalCode
                + ", country=" + country
                + ", type=" + type
                + ", primary=" + primary
                + '}';
    }
}
