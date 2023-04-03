package net.tirasa.connid.bundles.scim.v2.dto;

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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;

public class SCIMv2EnterpriseUser implements SCIMEnterpriseUser<SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager> {

    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

    @JsonProperty("employeeNumber")
    private String employeeNumber;

    @JsonProperty("costCenter")
    private String costCenter;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("division")
    private String division;

    @JsonProperty("department")
    private String department;


    @JsonProperty("manager")
    private SCIMv2EnterpriseUserManager manager;

    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public void setEmployeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    @Override
    public SCIMv2EnterpriseUserManager getManager() {
        return manager;
    }

    @Override
    public void setManager(final SCIMv2EnterpriseUserManager manager) {
        this.manager = manager;
    }

    @Override
    public Set<Attribute> toAttributes(final String id) throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();
        for (Field field : FieldUtils.getAllFieldsList(this.getClass()).stream().
                filter(f -> !"SCHEMA_URI".equals(f.getName()) && !"serialVersionUID".equals(f.getName()))
                .collect(Collectors.toList())) {
            if (SCIMv2EnterpriseUserManager.class.equals(field.getType()) && this.manager != null) {
                attrs.addAll(this.manager.toAttributes());
            } else if (!field.isAnnotationPresent(JsonIgnore.class)) {
                field.setAccessible(true);
                attrs.add(AttributeBuilder.build(id + "." + field.getName(), field.get(this)));
            }
        }
        return attrs;
    }

    public SCIMv2EnterpriseUser employeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
        return this;
    }

    public SCIMv2EnterpriseUser manager(final SCIMv2EnterpriseUserManager manager) {
        this.manager = manager;
        return this;
    }

    public static class SCIMv2EnterpriseUserManager implements Serializable {

        private static final long serialVersionUID = -7930518578899296192L;

        @JsonProperty("value")
        private String value;

        @JsonProperty("$ref")
        private String ref;

        @JsonProperty("displayName")
        private String displayName;

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(final String ref) {
            this.ref = ref;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("value", value)
                    .append("ref", ref)
                    .append("displayName", displayName)
                    .toString();
        }

        public SCIMv2EnterpriseUserManager value(String value) {
            this.value = value;
            return this;
        }

        public SCIMv2EnterpriseUserManager ref(String ref) {
            this.ref = ref;
            return this;
        }

        public SCIMv2EnterpriseUserManager displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public List<Attribute> toAttributes() {
            return Arrays.asList(AttributeBuilder.build(SCHEMA_URI + ".manager.value", this.value),
                    AttributeBuilder.build(SCHEMA_URI + ".manager.ref", this.ref),
                    AttributeBuilder.build(SCHEMA_URI + ".manager.displayName", this.displayName));
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("employeeNumber", employeeNumber)
                .append("costCenter", costCenter)
                .append("organization", organization)
                .append("division", division)
                .append("department", department)
                .append("manager", manager)
                .toString();
    }
}
