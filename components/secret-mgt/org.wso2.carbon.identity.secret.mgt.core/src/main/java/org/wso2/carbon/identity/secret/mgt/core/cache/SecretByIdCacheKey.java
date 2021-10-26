/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.secret.mgt.core.cache;

/**
 * Cache key for {@link org.wso2.carbon.identity.secret.mgt.core.model.Secret} caches by it's id.
 */
public class SecretByIdCacheKey extends SecretCacheKey {

    private static final long serialVersionUID = -8977919444443747515L;

    public SecretByIdCacheKey(String secretId) {

        super(secretId);
    }
}
