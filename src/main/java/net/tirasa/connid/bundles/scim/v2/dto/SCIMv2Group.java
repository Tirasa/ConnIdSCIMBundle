package net.tirasa.connid.bundles.scim.v2.dto;

import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;

import java.util.List;
import java.util.Set;

public class SCIMv2Group extends AbstractSCIMGroup<SCIMv2Meta> {

    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Group";

    public static final String RESOURCE_NAME = "Group";

    public SCIMv2Group() {
        super(SCHEMA_URI, new SCIMv2Meta(RESOURCE_NAME));
    }

    public static final class Builder {
        private SCIMv2Meta meta;
        private String id;
        private Set<String> schemas;
        private String displayName;
        private List<BaseResourceReference> members;
        private String baseSchema;

        public Builder() {
        }

        public Builder meta(SCIMv2Meta meta) {
            this.meta = meta;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder members(List<BaseResourceReference> members) {
            this.members = members;
            return this;
        }

        public Builder schemas(Set<String> schemas) {
            this.schemas = schemas;
            return this;
        }

        public Builder baseSchema(String baseSchema) {
            this.baseSchema = baseSchema;
            return this;
        }

        public SCIMv2Group build() {
            SCIMv2Group sCIMv2Group = new SCIMv2Group();
            sCIMv2Group.setMeta(meta);
            sCIMv2Group.setId(id);
            sCIMv2Group.setSchemas(schemas);
            sCIMv2Group.setDisplayName(displayName);
            sCIMv2Group.setSchemas(schemas);
            sCIMv2Group.baseSchema = this.baseSchema;
            sCIMv2Group.members = this.members;
            return sCIMv2Group;
        }
    }

    @Override
    public String toString() {
        return "SCIMv2Group{" +
                "displayName='" + displayName + '\'' +
                ", members=" + members +
                ", schemas=" + schemas +
                ", baseSchema='" + baseSchema + '\'' +
                ", meta=" + meta +
                ", id='" + id + '\'' +
                '}';
    }
}
