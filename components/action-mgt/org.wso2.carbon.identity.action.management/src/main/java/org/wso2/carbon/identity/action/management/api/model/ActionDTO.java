/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.action.management.api.model;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action Data Transfer Object.
 */
public class ActionDTO {

    private final String id;
    private final Action.ActionTypes type;
    private final String name;
    private final String description;
    private final Action.Status status;
    private final EndpointConfig endpoint;
    private final ActionRule rule;
    private final Map<String, Object> properties;

    public ActionDTO(Builder builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
        this.endpoint = builder.endpoint;
        this.rule = builder.rule;
        this.properties = builder.properties;
    }

    public ActionDTO(BuilderForService builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
        this.endpoint = builder.endpoint;
        this.rule = builder.rule;
        this.properties = builder.properties;
    }

    public ActionDTO(BuilderForData builder) {

        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.description = builder.description;
        this.status = builder.status;
        this.endpoint = builder.endpoint;
        this.rule = builder.rule;
        this.properties = builder.properties;
    }

    public String getId() {

        return id;
    }

    public Action.ActionTypes getType() {

        return type;
    }

    public String getName() {

        return name;
    }

    public String getDescription() {

        return description;
    }

    public Action.Status getStatus() {

        return status;
    }

    public EndpointConfig getEndpoint() {

        return endpoint;
    }

    public ActionRule getActionRule() {

        return rule;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public Object getProperty(String propertyName) {

        if (properties == null) {
            return null;
        }

        return properties.get(propertyName);
    }

    /**
     * Builder for ActionDTO.
     */
    public static class Builder {

        private final String id;
        private final Action.ActionTypes type;
        private final String name;
        private final String description;
        private final Action.Status status;
        private final EndpointConfig endpoint;
        private final ActionRule rule;
        private Map<String, Object> properties;

        public Builder(ActionDTO actionDTO) {

            this.id = actionDTO.getId();
            this.type = actionDTO.getType();
            this.name = actionDTO.getName();
            this.description = actionDTO.getDescription();
            this.status = actionDTO.getStatus();
            this.endpoint = actionDTO.getEndpoint();
            this.rule = actionDTO.getActionRule();
            this.properties = actionDTO.getProperties();
        }

        public Builder(Action action) {

            this.id = action.getId();
            this.type = action.getType();
            this.name = action.getName();
            this.description = action.getDescription();
            this.status = action.getStatus();
            this.endpoint = action.getEndpoint();
            this.rule = action.getActionRule();
        }

        public Builder properties(Map<String, Object> properties) {

            this.properties = properties;
            return this;
        }

        public ActionDTO build() {

            return new ActionDTO(this);
        }
    }

    /**
     * Builder for ActionDTO model used in Action Converter.
     */
    public static class BuilderForService {

        private final String id;
        private final Action.ActionTypes type;
        private final String name;
        private final String description;
        private final Action.Status status;
        private final EndpointConfig endpoint;
        private final ActionRule rule;
        private Map<String, Object> properties;

        public BuilderForService(ActionDTO actionDTO) {

            this.id = actionDTO.getId();
            this.type = actionDTO.getType();
            this.name = actionDTO.getName();
            this.description = actionDTO.getDescription();
            this.status = actionDTO.getStatus();
            this.endpoint = actionDTO.getEndpoint();
            this.rule = actionDTO.getActionRule();
            this.properties = actionDTO.getProperties();
        }

        public BuilderForService(Action action) {

            this.id = action.getId();
            this.type = action.getType();
            this.name = action.getName();
            this.description = action.getDescription();
            this.status = action.getStatus();
            this.endpoint = action.getEndpoint();
            this.rule = action.getActionRule();
        }

        public BuilderForService properties(Map<String, ActionPropertyForService> properties) {

            this.properties = properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            return this;
        }

        public ActionDTO build() {

            return new ActionDTO(this);
        }
    }

    /**
     * Builder for ActionDTO model used in DAO service impl.
     */
    public static class BuilderForData {

        private final String id;
        private final Action.ActionTypes type;
        private final String name;
        private final String description;
        private final Action.Status status;
        private final EndpointConfig endpoint;
        private final ActionRule rule;
        private Map<String, Object> properties;

        public BuilderForData(ActionDTO actionDTO) {

            this.id = actionDTO.getId();
            this.type = actionDTO.getType();
            this.name = actionDTO.getName();
            this.description = actionDTO.getDescription();
            this.status = actionDTO.getStatus();
            this.endpoint = actionDTO.getEndpoint();
            this.rule = actionDTO.getActionRule();
            this.properties = actionDTO.getProperties();
        }

        public BuilderForData properties(Map<String, ActionPropertyForDAO> properties) {

            this.properties = properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            return this;
        }

        public ActionDTO build() {

            return new ActionDTO(this);
        }
    }
}
