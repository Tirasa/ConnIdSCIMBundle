/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.BaseEntity;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;

public class PagedResults<T extends SCIMBaseResource> {

    @JsonProperty
    private int totalResults;

    @JsonProperty
    private int itemsPerPage;

    @JsonProperty
    private int startIndex;

    @JsonProperty
    private List<String> schemas;

    @JsonProperty("Resources")
    private List<T> resources = new ArrayList<>();

    public List<T> getResources() {
        return resources;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(final int totalResults) {
        this.totalResults = totalResults;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(final int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(final int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public String toString() {
        return "PagedResults{" + "totalResults=" + totalResults + ", itemsPerPage=" + itemsPerPage + ", startIndex="
                + startIndex + ", schemas=" + schemas + ", resources=" + resources + '}';
    }

}
