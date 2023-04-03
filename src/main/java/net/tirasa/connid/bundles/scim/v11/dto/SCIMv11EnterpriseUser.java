package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.SCIMDefaultComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv11EnterpriseUser implements SCIMEnterpriseUser<SCIMDefaultComplex> {

    @JsonProperty("employeeNumber")
    private String employeeNumber;

    @JsonProperty("manager")
    private SCIMDefaultComplex manager;

    @Override
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    @Override
    public void setEmployeeNumber(final String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    @Override
    public SCIMDefaultComplex getManager() {
        return manager;
    }

    @Override
    public void setManager(final SCIMDefaultComplex manager) {
        this.manager = manager;
    }

    @Override
    public Set<Attribute> toAttributes(String id) throws IllegalArgumentException, IllegalAccessException {
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
