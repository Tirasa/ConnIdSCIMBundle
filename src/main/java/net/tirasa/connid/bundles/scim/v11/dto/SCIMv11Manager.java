package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SCIMv11Manager implements Serializable {

    @JsonProperty
    private String managerId;

    @JsonProperty
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("managerId", managerId)
                .append("displayName", displayName)
                .toString();
    }
}
