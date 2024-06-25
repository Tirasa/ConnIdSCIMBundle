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
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.IMCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhotoCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMUserName;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

public abstract class AbstractSCIMUser<SAT extends SCIMBaseAttribute<SAT>, CT extends SCIMComplexAttribute,
        ET extends BaseResourceReference,
        MT extends SCIMBaseMeta, EUT extends SCIMEnterpriseUser<?>>
        extends AbstractSCIMBaseResource<MT> implements SCIMUser<MT, EUT> {

    private static final long serialVersionUID = 9147517308573800805L;

    protected Boolean active = true;

    protected List<SCIMUserAddress> addresses = new ArrayList<>();

    protected String displayName;

    protected List<SCIMGenericComplex<EmailCanonicalType>> emails = new ArrayList<>();

    protected List<ET> entitlements = new ArrayList<>();

    protected List<BaseResourceReference> groups = new ArrayList<>();

    protected List<SCIMGenericComplex<IMCanonicalType>> ims = new ArrayList<>();

    protected String locale;

    protected SCIMUserName name;

    protected String nickName;

    protected String password;

    protected List<SCIMGenericComplex<PhoneNumberCanonicalType>> phoneNumbers = new ArrayList<>();

    protected List<SCIMGenericComplex<PhotoCanonicalType>> photos = new ArrayList<>();

    protected String profileUrl;

    protected String preferredLanguage;

    protected List<CT> roles = new ArrayList<>();

    protected String timezone;

    protected String title;

    protected String userName;

    protected String userType;

    protected List<CT> x509Certificates = new ArrayList<>();

    protected String externalId;

    @JsonIgnore
    protected final Map<SAT, List<Object>> scimCustomAttributes = new HashMap<>();

    @JsonIgnore
    protected final Map<String, List<Object>> returnedCustomAttributes = new HashMap<>();

    public AbstractSCIMUser() {
    }

    protected AbstractSCIMUser(final String schemaUri, final MT meta) {
        super(meta);
        this.baseSchema = schemaUri;
        schemas.add(baseSchema);
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

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public List<SCIMGenericComplex<EmailCanonicalType>> getEmails() {
        return emails;
    }

    @Override
    public List<ET> getEntitlements() {
        return entitlements;
    }

    @Override
    public List<BaseResourceReference> getGroups() {
        return groups;
    }

    public List<SCIMGenericComplex<IMCanonicalType>> getIms() {
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

    public List<SCIMGenericComplex<PhoneNumberCanonicalType>> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<SCIMGenericComplex<PhotoCanonicalType>> getPhotos() {
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
    @Override
    public Map<SAT, List<Object>> getSCIMCustomAttributes() {
        return scimCustomAttributes;
    }

    @JsonIgnore
    @Override
    public Map<String, List<Object>> getReturnedCustomAttributes() {
        return returnedCustomAttributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setEmails(final List<SCIMGenericComplex<EmailCanonicalType>> emails) {
        this.emails = emails;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setEntitlements(final List<ET> entitlements) {
        this.entitlements = entitlements;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setGroups(final List<BaseResourceReference> groups) {
        this.groups = groups;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setIms(final List<SCIMGenericComplex<IMCanonicalType>> ims) {
        this.ims = ims;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setPhoneNumbers(final List<SCIMGenericComplex<PhoneNumberCanonicalType>> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setPhotos(final List<SCIMGenericComplex<PhotoCanonicalType>> photos) {
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
    public void fromAttributes(final Set<Attribute> attributes, final boolean replaceMembersOnUpdate) {
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

            case "emails.value":
                handleSCIMComplexObject(null, this.emails, s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.primary":
                handleSCIMComplexObject(null, this.emails, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.operation":
                handleSCIMComplexObject(null, this.emails, s -> s.setOperation(String.class.cast(value)));
                break;

            case "emails.work.value":
                handleSCIMComplexObject(EmailCanonicalType.work, this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.work.primary":
                handleSCIMComplexObject(EmailCanonicalType.work, this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.work.operation":
                handleSCIMComplexObject(EmailCanonicalType.work, this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "emails.home.value":
                handleSCIMComplexObject(EmailCanonicalType.home, this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.home.primary":
                handleSCIMComplexObject(EmailCanonicalType.home, this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.home.operation":
                handleSCIMComplexObject(EmailCanonicalType.home, this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "emails.other.value":
                handleSCIMComplexObject(EmailCanonicalType.other, this.emails,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "emails.other.primary":
                handleSCIMComplexObject(EmailCanonicalType.other, this.emails,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "emails.other.operation":
                handleSCIMComplexObject(EmailCanonicalType.other, this.emails,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.value":
                handleSCIMComplexObject(null, this.phoneNumbers, s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.primary":
                handleSCIMComplexObject(null, this.phoneNumbers, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.operation":
                handleSCIMComplexObject(null, this.phoneNumbers, s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.work.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.work, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.work.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.work, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.work.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.work, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.home.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.home, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.home.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.home, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.home.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.home, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.other.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.other, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.other.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.other, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.other.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.other, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.pager.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.pager, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.pager.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.pager, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.pager.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.pager, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.fax.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.fax, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.fax.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.fax, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.fax.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.fax, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "phoneNumbers.mobile.value":
                handleSCIMComplexObject(PhoneNumberCanonicalType.mobile, this.phoneNumbers,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "phoneNumbers.mobile.primary":
                handleSCIMComplexObject(PhoneNumberCanonicalType.mobile, this.phoneNumbers,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "phoneNumbers.mobile.operation":
                handleSCIMComplexObject(PhoneNumberCanonicalType.mobile, this.phoneNumbers,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.value":
                handleSCIMComplexObject(null, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.primary":
                handleSCIMComplexObject(null, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.operation":
                handleSCIMComplexObject(null, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.aim.value":
                handleSCIMComplexObject(IMCanonicalType.aim, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.aim.primary":
                handleSCIMComplexObject(IMCanonicalType.aim, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.aim.operation":
                handleSCIMComplexObject(IMCanonicalType.aim, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.xmpp.value":
                handleSCIMComplexObject(IMCanonicalType.xmpp, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.xmpp.primary":
                handleSCIMComplexObject(IMCanonicalType.xmpp, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.xmpp.operation":
                handleSCIMComplexObject(IMCanonicalType.xmpp, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.skype.value":
                handleSCIMComplexObject(IMCanonicalType.skype, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.skype.primary":
                handleSCIMComplexObject(IMCanonicalType.skype, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.skype.operation":
                handleSCIMComplexObject(IMCanonicalType.skype, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.qq.value":
                handleSCIMComplexObject(IMCanonicalType.qq, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.qq.primary":
                handleSCIMComplexObject(IMCanonicalType.qq, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.qq.operation":
                handleSCIMComplexObject(IMCanonicalType.qq, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.yahoo.value":
                handleSCIMComplexObject(IMCanonicalType.yahoo, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.yahoo.primary":
                handleSCIMComplexObject(IMCanonicalType.yahoo, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.yahoo.operation":
                handleSCIMComplexObject(IMCanonicalType.yahoo, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.msn.value":
                handleSCIMComplexObject(IMCanonicalType.msn, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.msn.primary":
                handleSCIMComplexObject(IMCanonicalType.msn, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.msn.operation":
                handleSCIMComplexObject(IMCanonicalType.msn, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "ims.icq.value":
                handleSCIMComplexObject(IMCanonicalType.icq, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.icq.primary":
                handleSCIMComplexObject(IMCanonicalType.icq, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.icq.operation":
                handleSCIMComplexObject(IMCanonicalType.icq, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;
            case "ims.gtalk.value":
                handleSCIMComplexObject(IMCanonicalType.gtalk, this.ims, s -> s.setValue(String.class.cast(value)));
                break;

            case "ims.gtalk.primary":
                handleSCIMComplexObject(IMCanonicalType.gtalk, this.ims, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "ims.gtalk.operation":
                handleSCIMComplexObject(IMCanonicalType.gtalk, this.ims, s -> s.setOperation(String.class.cast(value)));
                break;

            case "photos.photo.value":
                handleSCIMComplexObject(PhotoCanonicalType.photo, this.photos,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "photos.photo.primary":
                handleSCIMComplexObject(PhotoCanonicalType.photo, this.photos,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "photos.photo.operation":
                handleSCIMComplexObject(PhotoCanonicalType.photo, this.photos,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "photos.thumbnail.value":
                handleSCIMComplexObject(PhotoCanonicalType.thumbnail, this.photos,
                        s -> s.setValue(String.class.cast(value)));
                break;

            case "photos.thumbnail.primary":
                handleSCIMComplexObject(PhotoCanonicalType.thumbnail, this.photos,
                        s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "photos.thumbnail.operation":
                handleSCIMComplexObject(PhotoCanonicalType.thumbnail, this.photos,
                        s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.streetAddress":
                handleSCIMUserAddressObject(null, s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.locality":
                handleSCIMUserAddressObject(null, s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.formatted":
                handleSCIMUserAddressObject(null, s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.region":
                handleSCIMUserAddressObject(null, s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.postalCode":
                handleSCIMUserAddressObject(null, s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.country":
                handleSCIMUserAddressObject(null, s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.primary":
                handleSCIMUserAddressObject(null, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.operation":
                handleSCIMUserAddressObject(null, s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.work.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.work,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.work.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.work.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.work.region":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.work.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.work.country":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.work.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.work.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.work, s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.home.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.home,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.home.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.home.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.home.region":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.home.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.home.country":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.home.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.home.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.home, s -> s.setOperation(String.class.cast(value)));
                break;

            case "addresses.other.streetAddress":
                handleSCIMUserAddressObject(AddressCanonicalType.other,
                        s -> s.setStreetAddress(String.class.cast(value)));
                break;

            case "addresses.other.locality":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setLocality(String.class.cast(value)));
                break;

            case "addresses.other.formatted":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setFormatted(String.class.cast(value)));
                break;

            case "addresses.other.region":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setRegion(String.class.cast(value)));
                break;

            case "addresses.other.postalCode":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setPostalCode(String.class.cast(value)));
                break;

            case "addresses.other.country":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setCountry(String.class.cast(value)));
                break;

            case "addresses.other.primary":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setPrimary(Boolean.class.cast(value)));
                break;

            case "addresses.other.operation":
                handleSCIMUserAddressObject(AddressCanonicalType.other, s -> s.setOperation(String.class.cast(value)));
                break;

            case "roles.default.value":
                handleRoles(value);
                break;

            case "entitlements.default.value":
                handleDefaultEntitlement(value);
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

    protected abstract void handleDefaultEntitlement(Object value);

    @JsonIgnore
    protected <T extends Serializable> void handleSCIMComplexObject(final T type,
            final List<SCIMGenericComplex<T>> list, final Consumer<SCIMGenericComplex<T>> setter) {

        SCIMGenericComplex<T> selected = null;
        for (SCIMGenericComplex<T> complex : list) {
            if (complex.getType() != null && complex.getType().equals(type)) {
                selected = complex;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMGenericComplex<>();
            if (type != null) {
                selected.setType(type);
            }
            list.add(selected);
        }

        setter.accept(selected);
    }

    @JsonIgnore
    private void handleSCIMUserAddressObject(final AddressCanonicalType type, final Consumer<SCIMUserAddress> setter) {

        SCIMUserAddress selected = null;
        for (SCIMUserAddress complex : this.addresses) {
            if (complex.getType() != null && complex.getType().equals(type)) {
                selected = complex;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMUserAddress();
            if (type != null) {
                selected.setType(type);
            }
            this.addresses.add(selected);
        }

        setter.accept(selected);
    }

    @JsonIgnore
    private void addAttribute(final Set<Attribute> toAttrs, final Set<Attribute> attrs, final Class<?> type) {
        for (Attribute toAttribute : toAttrs) {
            attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(toAttribute.getValue(), toAttribute.getName(),
                    type).build());
        }
    }

    @Override
    @JsonProperty
    public String getExternalId() {
        return externalId;
    }

    @Override
    @JsonProperty
    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getBaseSchema() {
        return baseSchema;
    }

    @JsonIgnore
    @Override
    @SuppressWarnings("unchecked")
    public Set<Attribute> toAttributes(final Class<?> type, final SCIMConnectorConfiguration conf)
            throws IllegalArgumentException, IllegalAccessException {

        Set<Attribute> attrs = new HashSet<>();

        SCIMUtils.getAllFieldsList(type).stream().
                filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())
                && !"RESOURCE_NAME".equals(f.getName()) && !"SCHEMA_URI".equals(f.getName())).forEach(field -> {

            try {
                field.setAccessible(true);
                // manage enterprise user
                if (SCIMEnterpriseUser.class.isAssignableFrom(field.getType()) && getEnterpriseUser() != null) {
                    field.setAccessible(true);
                    addAttribute(getEnterpriseUser().toAttributes(SCIMv2EnterpriseUser.SCHEMA_URI), attrs,
                            field.getType());
                } else if (!field.isAnnotationPresent(JsonIgnore.class) && !SCIMUtils.isEmptyObject(field.get(this))) {
                    Object objInstance = field.get(this);

                    if (field.getGenericType().toString().contains(SCIMGenericComplex.class.getName())) {
                        if (field.getGenericType().toString().contains(PhoneNumberCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMGenericComplex<PhoneNumberCanonicalType>> list =
                                        (List<SCIMGenericComplex<PhoneNumberCanonicalType>>) objInstance;
                                for (SCIMGenericComplex<PhoneNumberCanonicalType> complex : list) {
                                    addAttribute(complex.toAttributes(
                                            SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS, conf),
                                            attrs, field.getType());
                                }
                            } else {
                                SCIMGenericComplex<PhoneNumberCanonicalType> complex =
                                        (SCIMGenericComplex<PhoneNumberCanonicalType>) objInstance;
                                addAttribute(
                                        complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS,
                                                conf),
                                        attrs, field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(IMCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMGenericComplex<IMCanonicalType>> list =
                                        (List<SCIMGenericComplex<IMCanonicalType>>) objInstance;
                                for (SCIMGenericComplex<IMCanonicalType> complex : list) {
                                    addAttribute(complex.toAttributes(SCIMAttributeUtils.SCIM_USER_IMS,
                                            conf),
                                            attrs, field.getType());
                                }
                            } else {
                                SCIMGenericComplex<IMCanonicalType> complex =
                                        (SCIMGenericComplex<IMCanonicalType>) objInstance;
                                addAttribute(complex.toAttributes(SCIMAttributeUtils.SCIM_USER_IMS,
                                        conf),
                                        attrs, field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(EmailCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMGenericComplex<EmailCanonicalType>> list =
                                        (List<SCIMGenericComplex<EmailCanonicalType>>) objInstance;
                                for (SCIMGenericComplex<EmailCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS,
                                                    conf),
                                            attrs, field.getType());
                                }
                            } else {
                                SCIMGenericComplex<EmailCanonicalType> complex =
                                        (SCIMGenericComplex<EmailCanonicalType>) objInstance;
                                addAttribute(complex.toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS,
                                        conf),
                                        attrs, field.getType());
                            }
                        } else if (field.getGenericType().toString().contains(PhotoCanonicalType.class.getName())) {
                            if (field.getType().equals(List.class)) {
                                List<SCIMGenericComplex<PhotoCanonicalType>> list =
                                        (List<SCIMGenericComplex<PhotoCanonicalType>>) objInstance;
                                for (SCIMGenericComplex<PhotoCanonicalType> complex : list) {
                                    addAttribute(
                                            complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS,
                                                    conf),
                                            attrs, field.getType());
                                }
                            } else {
                                SCIMGenericComplex<PhotoCanonicalType> complex =
                                        (SCIMGenericComplex<PhotoCanonicalType>) objInstance;
                                addAttribute(complex.toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS,
                                        conf),
                                        attrs, field.getType());
                            }
                        }
                    } else if (field.getGenericType().toString().contains(SCIMUserName.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMUserName> list = (List<SCIMUserName>) objInstance;
                            for (SCIMUserName scimUserName : list) {
                                addAttribute(scimUserName.toAttributes(), attrs, field.getType());
                            }
                        } else {
                            addAttribute(SCIMUserName.class.cast(objInstance).toAttributes(), attrs, field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(SCIMUserAddress.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMUserAddress> list = (List<SCIMUserAddress>) objInstance;
                            for (SCIMUserAddress scimUserAddress : list) {
                                addAttribute(scimUserAddress.toAttributes(conf), attrs, field.getType());
                            }
                        } else {
                            addAttribute(
                                    SCIMUserAddress.class.cast(objInstance).toAttributes(conf),
                                    attrs, field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(SCIMDefaultComplex.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<CT> list = (List<CT>) objInstance;
                            for (CT ct : list) {
                                String localId = null;
                                if (StringUtil.isNotBlank(ct.getValue())) {
                                    if (roles.contains(ct)) {
                                        localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                                    }
                                }
                                if (localId != null) {
                                    addAttribute(ct.toAttributes(localId, conf), attrs, field.getType());
                                }
                            }
                        } else {
                            CT ct = (CT) objInstance;
                            String localId = null;
                            if (StringUtil.isNotBlank(ct.getValue())) {
                                if (roles.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                                } else if (x509Certificates.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES;
                                } else if (groups.contains(ct)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_GROUPS;
                                }
                            }
                            if (localId != null) {
                                addAttribute(ct.toAttributes(localId, conf), attrs, field.getType());
                            }
                        }
                    } else if (field.getGenericType().toString().contains(SCIMBaseMeta.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<MT> list = (List<MT>) objInstance;
                            for (MT scimMeta : list) {
                                addAttribute(scimMeta.toAttributes(), attrs, field.getType());
                            }
                        } else {
                            addAttribute(SCIMBaseMeta.class.cast(objInstance).toAttributes(), attrs, field.getType());
                        }
                    } else if (SCIMAttributeUtils.SCIM_USER_GROUPS.equals(field.getName())) {
                        // manage groups
                        List<BaseResourceReference> groupRefs = (List<BaseResourceReference>) objInstance;
                        attrs.add(AttributeBuilder.build(SCIMAttributeUtils.SCIM_USER_GROUPS,
                                groupRefs.stream().map(g -> g.getValue()).collect(Collectors.toList())));
                    } else if (SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS.equals(field.getName())) {
                        // manage entitlements
                        entitlementsToAttribute((List<ET>) objInstance, attrs);
                    } else if (field.getType().equals(List.class)
                            && field.getGenericType() instanceof ParameterizedType) {
                        // properly manage lists with parametrized type
                        List<CT> complexTypeList = (List<CT>) objInstance;
                        switch (field.getName()) {
                            case SCIMAttributeUtils.SCIM_USER_ROLES:
                                complexTypeList.forEach(ct -> {
                                    try {
                                        addAttribute(
                                                ct.toAttributes(SCIMAttributeUtils.SCIM_USER_ROLES, conf),
                                                attrs, field.getType());
                                    } catch (IllegalAccessException e) {
                                        LOG.error("Unable to read by reflection [0]",
                                                SCIMAttributeUtils.SCIM_USER_ROLES);
                                    }
                                });
                                break;

                            case SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES:
                                complexTypeList.forEach(ct -> {
                                    try {
                                        addAttribute(
                                                ct.toAttributes(SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES, conf),
                                                attrs, field.getType());
                                    } catch (IllegalAccessException e) {
                                        LOG.error("Unable to read by reflection [0]",
                                                SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES);
                                    }
                                });
                                break;

                            default:
                                LOG.warn("Unable to match complex type of field [0] with any known type",
                                        field.getName());
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

    protected abstract void entitlementsToAttribute(List<ET> entitlementRefs, Set<Attribute> attrs);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "active=" + active
                + ", addresses=" + addresses
                + ", displayName=" + displayName
                + ", emails=" + emails
                + ", entitlements=" + entitlements
                + ", groups=" + groups
                + ", ims=" + ims
                + ", locale=" + locale
                + ", name=" + name
                + ", nickName=" + nickName
                + ", password=" + password
                + ", phoneNumbers=" + phoneNumbers
                + ", photos=" + photos
                + ", profileUrl=" + profileUrl
                + ", preferredLanguage=" + preferredLanguage
                + ", roles=" + roles
                + ", timezone=" + timezone
                + ", title=" + title
                + ", userName=" + userName
                + ", userType=" + userType
                + ", x509Certificates=" + x509Certificates
                + ", externalId=" + externalId
                + ", enterpriseUser=" + getEnterpriseUser()
                + ", scimCustomAttributes=" + scimCustomAttributes
                + ", returnedCustomAttributes=" + returnedCustomAttributes
                + '}';
    }
}
