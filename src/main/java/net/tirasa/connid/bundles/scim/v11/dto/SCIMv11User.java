/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.*;
import net.tirasa.connid.bundles.scim.common.dto.*;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.IMCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Service;
import net.tirasa.connid.bundles.scim.v11.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.v11.types.PhotoCanonicalType;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv11User extends AbstractSCIMBaseResource<Attribute, SCIMv11Meta>
        implements SCIMUser<Attribute, SCIMv11Meta> {

    @JsonProperty
    private String id;

    @JsonProperty
    private String externalId;

    @JsonProperty
    private String userName;

    @JsonProperty
    private SCIMUserName name;

    @JsonProperty
    private String password; // not returned from API

    @JsonProperty
    private String displayName;

    @JsonProperty
    private String nickName;

    @JsonProperty
    private String profileUrl;

    @JsonProperty
    private String title;

    @JsonProperty
    private String userType;

    @JsonProperty
    private String preferredLanguage;

    @JsonProperty
    private String locale;

    @JsonProperty
    private String timezone;

    @JsonProperty
    private Boolean active;

    @JsonProperty
    private final List<SCIMComplex<EmailCanonicalType>> emails = new ArrayList<>();

    @JsonProperty
    private final List<SCIMComplex<PhoneNumberCanonicalType>> phoneNumbers = new ArrayList<>();

    @JsonProperty
    private final List<SCIMComplex<IMCanonicalType>> ims = new ArrayList<>();

    @JsonProperty
    private final List<SCIMComplex<PhotoCanonicalType>> photos = new ArrayList<>();

    @JsonProperty
    private final List<SCIMUserAddress> addresses = new ArrayList<>();

    @JsonProperty
    private final List<SCIMDefault> groups = new ArrayList<>();

    @JsonProperty
    private final List<SCIMDefault> roles = new ArrayList<>();

    @JsonProperty
    private final List<SCIMDefault> entitlements = new ArrayList<>();

    @JsonProperty
    private final List<SCIMDefault> x509Certificates = new ArrayList<>();

    @JsonProperty
    private final List<String> schemas = new ArrayList<>();

    @JsonIgnore
    private final Map<SCIMv11Attribute, List<Object>> scimCustomAttributes = new HashMap<>();

    @JsonIgnore
    private final Map<String, List<Object>> returnedCustomAttributes = new HashMap<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public SCIMUserName getName() {
        return name;
    }

    public void setName(final SCIMUserName name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(final String nickName) {
        this.nickName = nickName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(final String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(final String userType) {
        this.userType = userType;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(final String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public SCIMv11Meta getMeta() {
        return meta;
    }

    public void setMeta(final SCIMv11Meta meta) {
        this.meta = meta;
    }

    public List<SCIMComplex<EmailCanonicalType>> getEmails() {
        return emails;
    }

    public List<SCIMComplex<PhoneNumberCanonicalType>> getPhoneNumbers() {
        return phoneNumbers;
    }

    public List<SCIMComplex<IMCanonicalType>> getIms() {
        return ims;
    }

    public List<SCIMComplex<PhotoCanonicalType>> getPhotos() {
        return photos;
    }

    public List<SCIMUserAddress> getAddresses() {
        return addresses;
    }

    public List<SCIMDefault> getX509Certificates() {
        return x509Certificates;
    }

    public List<SCIMDefault> getGroups() {
        return groups;
    }

    public List<SCIMDefault> getRoles() {
        return roles;
    }

    public List<SCIMDefault> getEntitlements() {
        return entitlements;
    }

    @JsonIgnore
    public Map<SCIMv11Attribute, List<Object>> getSCIMCustomAttributes() {
        return scimCustomAttributes;
    }

    @JsonIgnore
    public Map<String, List<Object>> getReturnedCustomAttributes() {
        return returnedCustomAttributes;
    }

    /**
     * Populate 'scimAttributes' map with custom attributes (taken from Connector configuration)
     * according to related values in current ConnId attributes
     *
     * @param attributes
     * @param customAttributesJSON
     */
    @JsonIgnore
    public void fillSCIMCustomAttributes(final Set<Attribute> attributes, final String customAttributesJSON) {
        SCIMSchema<SCIMv11Attribute> customAttributesObj = SCIMv11Service.extractSCIMSchemas(customAttributesJSON);
        if (customAttributesObj != null) {
            for (Attribute attribute : attributes) {
                if (!CollectionUtil.isEmpty(attribute.getValue())) {
                    for (SCIMv11Attribute customAttribute : customAttributesObj.getAttributes()) {
                        String externalAttributeName = customAttribute.getSchema()
                                .concat(".")
                                .concat(customAttribute.getName());
                        if (externalAttributeName.equals(attribute.getName())) {
                            scimCustomAttributes.put(customAttribute, attribute.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    @JsonIgnore
    @Override
    @SuppressWarnings("unchecked")
    public Set<Attribute> toAttributes() throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();

        Field[] fields = SCIMv11User.class.getDeclaredFields();
        for (Field field : fields) {
            Object objInstance = field.get(this);
            if (!field.isAnnotationPresent(JsonIgnore.class) && !SCIMUtils.isEmptyObject(objInstance)) {
                field.setAccessible(true);

                if (field.getGenericType().toString().contains(SCIMComplex.class.getName())) {
                    if (field.getGenericType().toString().contains(PhoneNumberCanonicalType.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            for (SCIMComplex<PhoneNumberCanonicalType> sCIMComplex : new ArrayList<>(
                                    (List<SCIMComplex<PhoneNumberCanonicalType>>) objInstance)) {
                                addAttribute(sCIMComplex
                                                .toAttributes(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS),
                                        attrs,
                                        field.getType());
                            }
                        } else {
                            SCIMComplex<PhoneNumberCanonicalType> sCIMComplex =
                                    (SCIMComplex<PhoneNumberCanonicalType>) objInstance;
                            addAttribute(sCIMComplex
                                            .toAttributes(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS),
                                    attrs,
                                    field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(IMCanonicalType.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMComplex<IMCanonicalType>> obj =
                                    new ArrayList<>((List<SCIMComplex<IMCanonicalType>>) objInstance);
                            for (SCIMComplex<IMCanonicalType> sCIMComplex : obj) {
                                addAttribute(sCIMComplex
                                                .toAttributes(SCIMAttributeUtils.SCIM_USER_IMS),
                                        attrs,
                                        field.getType());
                            }
                        } else {
                            SCIMComplex<IMCanonicalType> sCIMComplex =
                                    (SCIMComplex<IMCanonicalType>) objInstance;
                            addAttribute(sCIMComplex
                                            .toAttributes(SCIMAttributeUtils.SCIM_USER_IMS),
                                    attrs,
                                    field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(EmailCanonicalType.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMComplex<EmailCanonicalType>> obj =
                                    new ArrayList<>((List<SCIMComplex<EmailCanonicalType>>) objInstance);
                            for (SCIMComplex<EmailCanonicalType> sCIMComplex : obj) {
                                addAttribute(sCIMComplex
                                                .toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS),
                                        attrs,
                                        field.getType());
                            }
                        } else {
                            SCIMComplex<EmailCanonicalType> sCIMComplex =
                                    (SCIMComplex<EmailCanonicalType>) objInstance;
                            addAttribute(sCIMComplex
                                            .toAttributes(SCIMAttributeUtils.SCIM_USER_EMAILS),
                                    attrs,
                                    field.getType());
                        }
                    } else if (field.getGenericType().toString().contains(PhotoCanonicalType.class.getName())) {
                        if (field.getType().equals(List.class)) {
                            List<SCIMComplex<PhotoCanonicalType>> obj =
                                    new ArrayList<>((List<SCIMComplex<PhotoCanonicalType>>) objInstance);
                            for (SCIMComplex<PhotoCanonicalType> sCIMComplex : obj) {
                                addAttribute(sCIMComplex
                                                .toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS),
                                        attrs,
                                        field.getType());
                            }
                        } else {
                            SCIMComplex<PhotoCanonicalType> sCIMComplex =
                                    (SCIMComplex<PhotoCanonicalType>) objInstance;
                            addAttribute(sCIMComplex
                                            .toAttributes(SCIMAttributeUtils.SCIM_USER_PHOTOS),
                                    attrs,
                                    field.getType());
                        }
                    }
                } else if (field.getGenericType().toString().contains(SCIMUserName.class.getName())) {
                    if (field.getType().equals(List.class)) {
                        List<SCIMUserName> obj =
                                new ArrayList<>((List<SCIMUserName>) objInstance);
                        for (SCIMUserName sCIMUserName : obj) {
                            addAttribute(sCIMUserName.toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else {
                        addAttribute(SCIMUserName.class.cast(objInstance).toAttributes(),
                                attrs,
                                field.getType());
                    }
                } else if (field.getGenericType().toString().contains(SCIMUserAddress.class.getName())) {
                    if (field.getType().equals(List.class)) {
                        List<SCIMUserAddress> obj =
                                new ArrayList<>((List<SCIMUserAddress>) objInstance);
                        for (SCIMUserAddress sCIMUserAddress : obj) {
                            addAttribute(sCIMUserAddress.toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else {
                        addAttribute(SCIMUserAddress.class.cast(objInstance).toAttributes(),
                                attrs,
                                field.getType());
                    }
                } else if (field.getGenericType().toString().contains(SCIMDefault.class.getName())) {
                    if (field.getType().equals(List.class)) {
                        List<SCIMDefault> obj =
                                new ArrayList<>((List<SCIMDefault>) objInstance);
                        for (SCIMDefault sCIMDefault : obj) {
                            String localId = null;
                            if (StringUtil.isNotBlank(sCIMDefault.getValue())) {
                                if (entitlements.contains(sCIMDefault)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS;
                                } else if (roles.contains(sCIMDefault)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                                } else if (groups.contains(sCIMDefault)) {
                                    localId = SCIMAttributeUtils.SCIM_USER_GROUPS;
                                }
                            }
                            if (localId != null) {
                                addAttribute(sCIMDefault.toAttributes(localId),
                                        attrs,
                                        field.getType());
                            }
                        }
                    } else {
                        SCIMDefault obj = SCIMDefault.class.cast(objInstance);
                        String localId = null;
                        if (StringUtil.isNotBlank(obj.getValue())) {
                            if (entitlements.contains(obj)) {
                                localId = SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS;
                            } else if (roles.contains(obj)) {
                                localId = SCIMAttributeUtils.SCIM_USER_ROLES;
                            } else if (groups.contains(obj)) {
                                localId = SCIMAttributeUtils.SCIM_USER_GROUPS;
                            }
                        }
                        if (localId != null) {
                            addAttribute(obj.toAttributes(localId),
                                    attrs,
                                    field.getType());
                        }
                    }
                } else if (field.getGenericType().toString().contains(SCIMv11Meta.class.getName())) {
                    if (field.getType().equals(List.class)) {
                        List<SCIMv11Meta> obj =
                                new ArrayList<>((List<SCIMv11Meta>) objInstance);
                        for (SCIMv11Meta sCIMMeta : obj) {
                            addAttribute(sCIMMeta.toAttributes(),
                                    attrs,
                                    field.getType());
                        }
                    } else {
                        addAttribute(SCIMv11Meta.class.cast(objInstance).toAttributes(),
                                attrs,
                                field.getType());
                    }
                } else {
                    attrs.add(SCIMAttributeUtils.buildAttributeFromClassField(field, this).build());
                }
            }
        }

        return attrs;
    }

    @JsonIgnore
    private void addAttribute(final Set<Attribute> toAttrs,
                              final Set<Attribute> attrs,
                              final Class<?> type) {
        for (Attribute toAttribute : toAttrs) {
            attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(
                    toAttribute.getValue(),
                    toAttribute.getName(),
                    type).build());
        }
    }

    @JsonIgnore
    @Override
    public void fromAttributes(final Set<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if (!CollectionUtil.isEmpty(attribute.getValue())) {
                try {
                    doSetAttribute(attribute.getName(), attribute.getValue());
                } catch (Exception e) {
                    LOG.warn(e, "While populating User field from ConnId attribute: {0}", attribute);
                }
            }
        }
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
            case "meta.attributes":
                if (meta == null) {
                    meta = new SCIMv11Meta();
                }
                this.meta.getAttributes().addAll(new ArrayList<>((List<String>) (Object) values));
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

            case "emails.work.value": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "emails.work.primary": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "emails.work.operation": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.work,
                        this.emails);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "emails.home.value": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "emails.home.primary": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "emails.home.operation": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.home,
                        this.emails);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "emails.other.value": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "emails.other.primary": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "emails.other.operation": {
                SCIMComplex<EmailCanonicalType> selected = handleSCIMComplexObject(
                        EmailCanonicalType.other,
                        this.emails);
                selected.setOperation(String.class.cast(value));
                break;
            }

            case "phoneNumbers.work.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.work.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.work.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.work,
                        this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "phoneNumbers.home.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.home.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.home.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.home,
                        this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "phoneNumbers.other.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.other.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.other.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.other,
                        this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "phoneNumbers.pager.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.pager.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.pager.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.pager,
                        this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "phoneNumbers.fax.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.fax.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.fax.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.fax,
                        this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "phoneNumbers.mobile.value": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.mobile,
                        this.phoneNumbers);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "phoneNumbers.mobile.primary": {
                SCIMComplex<PhoneNumberCanonicalType> selected = handleSCIMComplexObject(
                        PhoneNumberCanonicalType.mobile,
                        this.phoneNumbers);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "phoneNumbers.mobile.operation": {
                SCIMComplex<PhoneNumberCanonicalType> selected =
                        handleSCIMComplexObject(PhoneNumberCanonicalType.mobile,
                                this.phoneNumbers);
                selected.setOperation(String.class.cast(value));
                break;
            }

            case "ims.aim.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.aim.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.aim.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.aim,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.xmpp.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.xmpp.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.xmpp.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.xmpp,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.skype.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.skype.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.skype.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.skype,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.qq.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.qq.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.qq.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.qq,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.yahoo.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.yahoo.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.yahoo.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.yahoo,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.msn.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.msn.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.msn.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.msn,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.icq.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.icq.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.icq.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.icq,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "ims.gtalk.value": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "ims.gtalk.primary": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "ims.gtalk.operation": {
                SCIMComplex<IMCanonicalType> selected = handleSCIMComplexObject(
                        IMCanonicalType.gtalk,
                        this.ims);
                selected.setOperation(String.class.cast(value));
                break;
            }

            case "photos.photo.value": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "photos.photo.primary": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "photos.photo.operation": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.photo,
                        this.photos);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "photos.thumbnail.value": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "photos.thumbnail.primary": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "photos.thumbnail.operation": {
                SCIMComplex<PhotoCanonicalType> selected = handleSCIMComplexObject(
                        PhotoCanonicalType.thumbnail,
                        this.photos);
                selected.setOperation(String.class.cast(value));
                break;
            }

            case "addresses.work.streetAddress": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setStreetAddress(String.class.cast(value));
                break;
            }
            case "addresses.work.locality": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setLocality(String.class.cast(value));
                break;
            }
            case "addresses.work.formatted": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setFormatted(String.class.cast(value));
                break;
            }
            case "addresses.work.region": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setRegion(String.class.cast(value));
                break;
            }
            case "addresses.work.postalCode": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setPostalCode(String.class.cast(value));
                break;
            }
            case "addresses.work.country": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setCountry(String.class.cast(value));
                break;
            }
            case "addresses.work.primary": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "addresses.work.operation": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.work);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "addresses.home.streetAddress": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setStreetAddress(String.class.cast(value));
                break;
            }
            case "addresses.home.locality": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setLocality(String.class.cast(value));
                break;
            }
            case "addresses.home.formatted": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setFormatted(String.class.cast(value));
                break;
            }
            case "addresses.home.region": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setRegion(String.class.cast(value));
                break;
            }
            case "addresses.home.postalCode": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setPostalCode(String.class.cast(value));
                break;
            }
            case "addresses.home.country": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setCountry(String.class.cast(value));
                break;
            }
            case "addresses.home.primary": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "addresses.home.operation": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.home);
                selected.setOperation(String.class.cast(value));
                break;
            }
            case "addresses.other.streetAddress": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setStreetAddress(String.class.cast(value));
                break;
            }
            case "addresses.other.locality": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setLocality(String.class.cast(value));
                break;
            }
            case "addresses.other.formatted": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setFormatted(String.class.cast(value));
                break;
            }
            case "addresses.other.region": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setRegion(String.class.cast(value));
                break;
            }
            case "addresses.other.postalCode": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setPostalCode(String.class.cast(value));
                break;
            }
            case "addresses.other.country": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setCountry(String.class.cast(value));
                break;
            }
            case "addresses.other.primary": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setPrimary(Boolean.class.cast(value));
                break;
            }
            case "addresses.other.operation": {
                SCIMUserAddress selected = handleSCIMUserAddressObject(AddressCanonicalType.other);
                selected.setOperation(String.class.cast(value));
                break;
            }

            case "roles.default.value": {
                SCIMDefault selected = handleSCIMDefaultObject(
                        String.class.cast(value),
                        this.roles);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "entitlements.default.value": {
                SCIMDefault selected = handleSCIMDefaultObject(
                        String.class.cast(value),
                        this.entitlements);
                selected.setValue(String.class.cast(value));
                break;
            }
            case "x509Certificates.default.value": {
                SCIMDefault selected = handleSCIMDefaultObject(
                        String.class.cast(value),
                        this.x509Certificates);
                selected.setValue(String.class.cast(value));
                break;
            }

            case "schemas":
                this.schemas.addAll(new ArrayList<>((List<String>) (Object) values));
                break;

            default:
                break;
        }
    }

    @JsonIgnore
    private SCIMDefault handleSCIMDefaultObject(final String value, final List<SCIMDefault> list) {
        SCIMDefault selected = null;
        for (SCIMDefault scimDefault : list) {
            if (scimDefault.getValue().equals(value)) {
                selected = scimDefault;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMDefault();
            list.add(selected);
        }
        return selected;
    }

    @JsonIgnore
    private <T extends Enum<?>> SCIMComplex<T> handleSCIMComplexObject(final T type,
                                                                       final List<SCIMComplex<T>> list) {
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
        return selected;
    }

    @JsonIgnore
    private SCIMUserAddress handleSCIMUserAddressObject(final AddressCanonicalType type) {
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
        return selected;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", externalId=" + externalId + ", meta=" + meta + ", userName=" + userName
                + ", name=" + name + ", password=" + password + ", displayName=" + displayName + ", nickName="
                + nickName + ", profileUrl=" + profileUrl + ", title=" + title + ", userType=" + userType
                + ", preferredLanguage=" + preferredLanguage + ", locale=" + locale + ", timezone=" + timezone
                + ", active=" + active + ", emails=" + emails + ", phoneNumbers=" + phoneNumbers + ", ims=" + ims
                + ", photos=" + photos + ", groups=" + groups + ", roles=" + roles + ", addresses=" + addresses
                + ", x509Certificates=" + x509Certificates + ", schemas=" + schemas + ", entitlements=" + entitlements
                + ", scimCustomAttributes=" + scimCustomAttributes + ", returnedCustomAttributes="
                + returnedCustomAttributes + '}';
    }

}
