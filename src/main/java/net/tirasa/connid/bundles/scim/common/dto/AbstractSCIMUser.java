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
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.IMCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhotoCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMDefault;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMUserName;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public abstract class AbstractSCIMUser<
        SAT extends SCIMBaseAttribute<SAT>, GT extends Serializable, CT extends SCIMDefault, MT extends SCIMBaseMeta>
        extends AbstractSCIMBaseResource<Attribute, MT> implements SCIMUser<Attribute, MT> {

    private static final long serialVersionUID = 9147517308573800805L;

    protected Boolean active = true;

    protected List<SCIMUserAddress> addresses = new ArrayList<>();

    protected String displayName;

    protected List<SCIMComplex<EmailCanonicalType>> emails = new ArrayList<>();

    protected List<CT> entitlements = new ArrayList<>();

    protected List<GT> groups = new ArrayList<>();

    protected List<SCIMComplex<IMCanonicalType>> ims = new ArrayList<>();

    protected String locale;

    protected SCIMUserName name;

    protected String nickName;

    protected String password;

    protected List<SCIMComplex<PhoneNumberCanonicalType>> phoneNumbers = new ArrayList<>();

    protected List<SCIMComplex<PhotoCanonicalType>> photos = new ArrayList<>();

    protected String profileUrl;

    protected String preferredLanguage;

    protected List<CT> roles = new ArrayList<>();

    protected String timezone;

    protected String title;

    protected String userName;

    protected String userType;

    protected List<CT> x509Certificates = new ArrayList<>();

    @JsonIgnore
    protected final Map<SAT, List<Object>> scimCustomAttributes = new HashMap<>();

    @JsonIgnore
    protected final Map<String, List<Object>> returnedCustomAttributes = new HashMap<>();

    protected AbstractSCIMUser() {
    }

    protected AbstractSCIMUser(final String schemaUri, final String resourceName, final MT meta) {
        super(schemaUri, resourceName, meta);
    }

    public Boolean getActive() {
        return active;
    }

    @Override
    public void setActive(final Boolean active) {
        this.active = active;
    }

    public List<SCIMUserAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<SCIMUserAddress> addresses) {
        this.addresses = addresses;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public List<SCIMComplex<EmailCanonicalType>> getEmails() {
        return emails;
    }

    public List<CT> getEntitlements() {
        return entitlements;
    }

    public List<GT> getGroups() {
        return groups;
    }

    public List<SCIMComplex<IMCanonicalType>> getIms() {
        return ims;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public SCIMUserName getName() {
        return name;
    }

    public void setName(final SCIMUserName name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(final String nickName) {
        this.nickName = nickName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    public List<SCIMComplex<PhoneNumberCanonicalType>> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<SCIMComplex<PhotoCanonicalType>> getPhotos() {
        return photos;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(final String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public List<CT> getRoles() {
        return roles;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(final String userType) {
        this.userType = userType;
    }

    @JsonIgnore
    public Map<SAT, List<Object>> getSCIMCustomAttributes() {
        return scimCustomAttributes;
    }

    @JsonIgnore
    @Override
    public Map<String, List<Object>> getReturnedCustomAttributes() {
        return returnedCustomAttributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setEmails(final List<SCIMComplex<EmailCanonicalType>> emails) {
        this.emails = emails;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setEntitlements(final List<CT> entitlements) {
        this.entitlements = entitlements;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setGroups(final List<GT> groups) {
        this.groups = groups;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setIms(final List<SCIMComplex<IMCanonicalType>> ims) {
        this.ims = ims;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setPhoneNumbers(final List<SCIMComplex<PhoneNumberCanonicalType>> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setPhotos(final List<SCIMComplex<PhotoCanonicalType>> photos) {
        this.photos = photos;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setRoles(final List<CT> roles) {
        this.roles = roles;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setX509Certificates(final List<CT> x509Certificates) {
        this.x509Certificates = x509Certificates;
    }

    @JsonIgnore
    @Override
    public void fromAttributes(final Set<Attribute> attributes) {
        attributes.stream().filter(attribute -> !CollectionUtil.isEmpty(attribute.getValue())).forEach(attribute -> {
            try {
                doSetAttribute(attribute.getName(), attribute.getValue());
            } catch (Exception e) {
                LOG.warn(e, "While populating User field from ConnId attribute: {0}", attribute);
            }
        });
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    private void doSetAttribute(final String name, final List<Object> values) {
        Object value = values.get(0);

        // only NON-READ-ONLY fields here
        switch (name) {
            case "userName":
                this.userName = String.class.cast(value);
                break;
            case "name.formatted":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setFormatted(String.class.cast(value));
                break;
            case "name.familyName":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setFamilyName(String.class.cast(value));
                break;
            case "name.givenName":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setGivenName(String.class.cast(value));
                break;
            case "name.middleName":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setMiddleName(String.class.cast(value));
                break;
            case "name.honorificPrefix":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setHonorificPrefix(String.class.cast(value));
                break;
            case "name.honorificSuffix":
                if (this.name == null) {
                    this.name = new SCIMUserName();
                }
                this.name.setHonorificSuffix(String.class.cast(value));
                break;
            case "displayName":
                this.displayName = String.class.cast(value);
                break;
            case "nickName":
                this.nickName = String.class.cast(value);
                break;
            case "profileUrl":
                this.profileUrl = String.class.cast(value);
                break;
            case "title":
                this.title = String.class.cast(value);
                break;
            case "userType":
                this.userType = String.class.cast(value);
                break;
            case "locale":
                this.locale = String.class.cast(value);
                break;
            case "preferredLanguage":
                this.preferredLanguage = String.class.cast(value);
                break;
            case "timezone":
                this.timezone = String.class.cast(value);
                break;
            case "active":
                this.active = Boolean.class.cast(value);
                break;

            case "emails.work.value":
                handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.work.primary":
                handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.work.operation":
                handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "emails.home.value":
                handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.home.primary":
                handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.home.operation":
                handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "emails.other.value":
                handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.other.primary":
                handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.other.operation":
                handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.work.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.work.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.work.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.home.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.home.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.home.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.other.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.other.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.other.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.pager.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.pager.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.pager.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.fax.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.fax.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.fax.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.mobile.value":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.mobile,
                        this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.mobile.primary":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.mobile,
                        this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.mobile.operation":
                handleSCIMComplexObject(
                        PhoneNumberCanonicalType.mobile,
                        this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.aim.value":
                handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.aim.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.aim.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.xmpp.value":
                handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.xmpp.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.xmpp.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.skype.value":
                handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.skype.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.skype.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.qq.value":
                handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.qq.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.qq.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.yahoo.value":
                handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.yahoo.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.yahoo.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.msn.value":
                handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.msn.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.msn.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.icq.value":
                handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.icq.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.icq.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;
            case "ims.gtalk.value":
                handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.gtalk.primary":
                handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.gtalk.operation":
                handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "photos.photo.value":
                handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "photos.photo.primary":
                handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "photos.photo.operation":
                handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "photos.thumbnail.value":
                handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "photos.thumbnail.primary":
                handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "photos.thumbnail.operation":
                handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.work.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.work.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.work.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.work.region":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.work.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.work.country":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.work.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.work.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.home.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.home.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.home.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.home.region":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.home.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.home.country":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.home.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.home.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.other.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.other.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.other.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.other.region":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.other.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.other.country":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.other.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.other.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "roles.default.value":
                handleRoles(value);
                break;

            case "entitlements.default.value":
                handleEntitlements(value);
                break;

            case "x509Certificates.default.value":
                handlex509Certificates(value);
                break;

            case "schemas":
                this.schemas.addAll(new ArrayList<>((List<String>) (Object) values));
                break;

            default:
                break;
        }
    }

    protected abstract void handleRoles(Object value);

    protected abstract void handlex509Certificates(Object value);

    protected abstract void handleEntitlements(Object value);

    @JsonIgnore
    protected <T extends Serializable> void handleSCIMComplexObject(
            final T type, final List<SCIMComplex<T>> list, final Consumer<SCIMComplex<T>> setter) {

        SCIMComplex<T> selected = null;
        for (SCIMComplex<T> complex : list) {
            if (complex.getType().equals(type)) {
                selected = complex;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMComplex<>();
            selected.setType(type);
            list.add(selected);
        }

        setter.accept(selected);
    }

    @JsonIgnore
    private void handleSCIMUserAddressObject(
            final AddressCanonicalType type, final Consumer<SCIMUserAddress> setter) {

        SCIMUserAddress selected = null;
        for (SCIMUserAddress complex : this.addresses) {
            if (complex.getType().equals(type)) {
                selected = complex;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMUserAddress();
            selected.setType(type);
            this.addresses.add(selected);
        }

        setter.accept(selected);
    }

    @JsonIgnore
    private void addAttribute(final Set<Attribute> toAttrs, final Set<Attribute> attrs, final Class<?> type) {
        for (Attribute toAttribute : toAttrs) {
            attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(
                    toAttribute.getValue(),
                    toAttribute.getName(),
                    type).build());
        }
    }

    @JsonIgnore
    @Override
    @SuppressWarnings("unchecked")
    public Set<Attribute> toAttributes(final Class<?> type) throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();

        FieldUtils.getAllFieldsList(type).stream().
                filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())).forEach(field -> {

            try {
                Object objInstance = field.get(this);
                if (!field.isAnnotationPresent(JsonIgnore.class) && !SCIMUtils.isEmptyObject(objInstance)) {
                    field.setAccessible(true);

                    if (field.getGenericType().toString().contains(SCIMComplex.class.getName())) {
                        if (field.getGenericType().toString().contains(PhoneNumberCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMComplex<PhoneNumberCanonicalType>> list =
                                        (List<SCIMComplex<PhoneNumberCanonicalType>>) objInstance;
                                for (SCIMComplex<PhoneNumberCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS),
                                            attrs,
                                            field.getType());
                                }
                            } else {
                                SCIMComplex<PhoneNumberCanonicalType> complex =
                                        (SCIMComplex<PhoneNumberCanonicalType>) objInstance;
                                addAttribute(
                                        complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS),
                                        attrs,
                                        field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(IMCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMComplex<IMCanonicalType>> list =
                                        (List<SCIMComplex<IMCanonicalType>>) objInstance;
                                for (SCIMComplex<IMCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_IMS),
                                            attrs,
                                            field.getType());
                                }
                            } else {
                                SCIMComplex<IMCanonicalType> complex = (SCIMComplex<IMCanonicalType>) objInstance;
                                addAttribute(
                                        complex.toAttributes(SCIMAttributeUtils.SCIM_USER_IMS),
                                        attrs,
                                        field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(EmailCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMComplex<EmailCanonicalType>> list =
                                        (List<SCIMComplex<EmailCanonicalType>>) objInstance;
                                for (SCIMComplex<EmailCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS),
                                            attrs,
                                            field.getType());
                                }
                            } else {
                                SCIMComplex<EmailCanonicalType> complex =
                                        (SCIMComplex<EmailCanonicalType>) objInstance;
                                addAttribute(
                                        complex.toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS),
                                        attrs,
                                        field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(PhotoCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMComplex<PhotoCanonicalType>> list =
                                        (List<SCIMComplex<PhotoCanonicalType>>) objInstance;
                                for (SCIMComplex<PhotoCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS),
                                            attrs,
                                            field.getType());
                                }
                            } else {
                                SCIMComplex<PhotoCanonicalType> complex =
                                        (SCIMComplex<PhotoCanonicalType>) objInstance;
                                addAttribute(
                                        complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS),
                                        attrs,
                                        field.getType());
                            }
                        }
                    } else if (field.getGenericType().toString().contains(SCIMUserName.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMUserName> list = (List<SCIMUserName>) objInstance;
                            for (SCIMUserName scimUserName : list) {
                                addAttribute(scimUserName.toAttributes(), attrs, field.getType());
                            }
                        } else {
                            addAttribute(
                                    SCIMUserName.class.cast(objInstance).toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(SCIMUserAddress.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMUserAddress> list = (List<SCIMUserAddress>) objInstance;
                            for (SCIMUserAddress scimUserAddress : list) {
                                addAttribute(scimUserAddress.toAttributes(), attrs, field.getType());
                            }
                        } else {
                            addAttribute(
                                    SCIMUserAddress.class.cast(objInstance).toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(SCIMDefault.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<CT> list = (List<CT>) objInstance;
                            for (CT ct : list) {
                                String localId = null;
                                if (StringUtil.isNotBlank(ct.getValue())) {
                                    if (entitlements.contains(ct)) {
                                        localId = SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS;
                                    } else if (roles.contains(ct)) {
                                        localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                                    } else if (groups.contains(ct)) {
                                        localId = SCIMAttributeUtils.SCIM_USER_GROUPS;
                                    }
                                }
                                if (localId != null) {
                                    addAttribute(ct.toAttributes(localId), attrs, field.getType());
                                }
                            }
                        } else {
                            CT ct = (CT) objInstance;
                            String localId = null;
                            if (StringUtil.isNotBlank(ct.getValue())) {
                                if (entitlements.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS;
                                } else if (roles.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                                } else if (groups.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_GROUPS;
                                }
                            }
                            if (localId != null) {
                                addAttribute(
                                        ct.toAttributes(localId),
                                        attrs,
                                        field.getType());
                            }
                        }
                    } else if (field.getGenericType().toString().contains(SCIMBaseMeta.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<MT> list = (List<MT>) objInstance;
                            for (MT scimMeta : list) {
                                addAttribute(
                                        scimMeta.toAttributes(),
                                        attrs,
                                        field.getType());
                            }
                        } else {
                            addAttribute(
                                    SCIMBaseMeta.class.cast(objInstance).toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else {
                        attrs.add(SCIMAttributeUtils.buildAttributeFromClassField(field, this).build());
                    }
                }
            } catch (IllegalAccessException e) {
                LOG.error("Unable to build user attributes by reflection", e);
            }
        });

        return attrs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("active", active)
                .append("addresses", addresses)
                .append("displayName", displayName)
                .append("emails", emails)
                .append("entitlements", entitlements)
                .append("groups", groups)
                .append("ims", ims)
                .append("locale", locale)
                .append("name", name)
                .append("nickName", nickName)
                .append("phoneNumbers", phoneNumbers)
                .append("photos", photos)
                .append("profileUrl", profileUrl)
                .append("preferredLanguage", preferredLanguage)
                .append("roles", roles)
                .append("timezone", timezone)
                .append("title", title)
                .append("userName", userName)
                .append("userType", userType)
                .append("x509Certificates", x509Certificates)
                .append("scimCustomAttributes", scimCustomAttributes)
                .toString();
    }
}
