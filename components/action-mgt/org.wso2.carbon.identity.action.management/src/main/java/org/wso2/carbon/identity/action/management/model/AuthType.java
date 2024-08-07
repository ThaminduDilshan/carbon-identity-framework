/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.model;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthType.
 */
public class AuthType {

    /**
     * Authentication Type.
     */
    public enum AuthenticationType {

        NONE("NONE", new String[]{}),
        BEARER("BEARER", new String[]{"accessToken"}),
        BASIC("BASIC", new String[]{"username", "password"}),
        API_KEY("API_KEY", new String[]{"header", "value"});

        private final String type;
        private final String[] properties;

        AuthenticationType(String type, String[]  properties) {

            this.type = type;
            this.properties = properties;
        }

        public String getType() {

            return type;
        }

        public String[] getProperties() {

            return properties;
        }
    }

    private AuthenticationType type;
    private Map<String, Object> properties = null;

    public AuthType() {
    }

    public AuthType(AuthTypeBuilder authTypeBuilder) {

        this.type = authTypeBuilder.type;
        this.properties = authTypeBuilder.properties;
    }

    public AuthenticationType getType() {

        return type;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    /**
     * AuthType builder.
     */
    public static class AuthTypeBuilder {

        private AuthenticationType type;
        private Map<String, Object> properties = null;

        public AuthTypeBuilder() {
        }

        public AuthTypeBuilder type(AuthenticationType type) {

            this.type = type;
            return this;
        }

        public AuthTypeBuilder properties(Map<String, Object> properties) {

            this.properties = properties;
            return this;
        }

        public AuthTypeBuilder addProperty(String key, String value) {

            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            this.properties.put(key, value);
            return this;
        }

        public AuthType build() {

            return new AuthType(this);
        }
    }
}
