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

package org.wso2.carbon.identity.secret.mgt.core;

import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.internal.SecretManagerComponentDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.*;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.*;

/**
 * Secret Manager service implementation.
 */
public class SecretManagerImpl implements SecretManager {

    private static final Log log = LogFactory.getLog(SecretManagerImpl.class);
    private final List<SecretDAO> secretDAOS;

    public SecretManagerImpl() {

        this.secretDAOS = SecretManagerComponentDataHolder.getInstance().getSecretDAOS();
    }

    @Override
    public Secret addSecret(String secretTypeName, Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretCreateRequest(secretTypeName, secret);
        String secretId = generateUniqueID();
        secret.setSecretId(secretId);
        secret.setSecretType(secretTypeName);
        secret.setSecretValue(getEncryptedSecret(secret.getSecretValue(), secret.getSecretName()));
        this.getSecretDAO().addSecret(secret);
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretName() + " added successfully");
        }
        return secret;
    }

    @Override
    public Secret getSecret(String secretTypeName, String secretName) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretRetrieveRequest(secretTypeName, secretName);
        SecretType secretType = getSecretType(secretTypeName);
        Secret secret = this.getSecretDAO().getSecretByName(secretName, secretType, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretName: " + secretName);
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secretName + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public Secrets getSecrets(String secretTypeName) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretType(secretTypeName);

        SecretType secretType = getSecretType(secretTypeName);
        List secretList = this.getSecretDAO().getSecrets(secretType, getTenantId());
        if (secretList == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretTypeName: " + secretTypeName + "for the tenant: " + getTenantDomain());
            }
            throw handleClientException(
                    ERROR_CODE_SECRETS_DOES_NOT_EXISTS, null);
        }
        if (log.isDebugEnabled()) {
            log.debug("All secrets of tenant: " + getTenantDomain() + " are retrieved successfully.");
        }
        return new Secrets(secretList);
    }

    @Override
    public Secret getSecretById(String secretId) throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        Secret secret = this.getSecretDAO().getSecretById(secretId, getTenantId());
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretId: " + secretId);
            }
            throw handleClientException(ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS, secretId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Secret: " + secret.getSecretId() + " is retrieved successfully.");
        }
        return secret;
    }

    @Override
    public void deleteSecret(String secretTypeName, String secretName) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretDeleteRequest(secretTypeName, secretName);
        SecretType secretType = getSecretType(secretTypeName);
        if (isSecretExist(secretTypeName, secretName)) {
            this.getSecretDAO().deleteSecretByName(secretName, secretType.getName(), getTenantId());
            if (log.isDebugEnabled()) {
                log.debug("Secret: " + secretName + " is deleted successfully.");
            }
        } else {
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName);
        }
    }

    @Override
    public void deleteSecretById(String secretId) throws SecretManagementException {

        validateSecretManagerEnabled();
        if (StringUtils.isBlank(secretId)) {
            throw handleClientException(ERROR_CODE_INVALID_SECRET_ID, secretId);
        }
        if (isSecretExistById(secretId)) {
            this.getSecretDAO().deleteSecretById(secretId, getTenantId());
            if (log.isDebugEnabled()) {
                log.debug("Secret id: " + secretId + " in tenant: " + getTenantDomain() + " deleted successfully.");
            }
        } else {
            throw handleClientException(ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS, secretId);
        }
    }

    @Override
    public Secret replaceSecret(String secretTypeName, Secret secret) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretReplaceRequest(secretTypeName, secret);
        secret.setSecretName(getSecretById(secret.getSecretId()).getSecretName());
        secret.setSecretType(secretTypeName);
        secret.setSecretValue(getEncryptedSecret(secret.getSecretValue(), secret.getSecretName()));
        this.getSecretDAO().replaceSecret(secret);
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretName() + " secret replaced successfully.");
        }
        return secret;
    }

    @Override
    public Secret updateSecretValue(String secretTypeName, String name, String value) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretValue(value);

        Secret secret, updatedSecret;
        secret = getSecret(secretTypeName, name);
        try {
            updatedSecret = this.getSecretDAO().updateSecretValue(secret, encrypt(value));
        } catch (CryptoException e) {
            throw handleServerException(ERROR_CODE_UPDATE_SECRET, value, e);
        }
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretName() + " secret value updated successfully.");
        }
        return updatedSecret;
    }

    @Override
    public Secret updateSecretValueById(String secretId, String value) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretValue(value);

        Secret secret, updatedSecret;
        secret = getSecretById(secretId);
        try {
            updatedSecret = this.getSecretDAO().updateSecretValue(secret, encrypt(value));
        } catch (CryptoException e) {
            throw handleServerException(ERROR_CODE_UPDATE_SECRET, value, e);
        }
        if (log.isDebugEnabled()) {
            log.debug(secret.getSecretName() + " secret value updated successfully.");
        }
        return updatedSecret;
    }

    @Override
    public Secret updateSecretDescription(String secretTypeName, String name, String description) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretDescription(description);

        Secret secret = getSecret(secretTypeName, name);
        Secret updatedSecret = this.getSecretDAO().updateSecretDescription(secret, description);
        if (log.isDebugEnabled()) {
            log.debug(name + "secret description updated successfully.");
        }
        return updatedSecret;
    }

    @Override
    public Secret updateSecretDescriptionById(String secretId, String description) throws SecretManagementException {

        validateSecretManagerEnabled();
        validateSecretDescription(description);

        Secret secret = getSecretById(secretId);
        Secret updatedSecret = this.getSecretDAO().updateSecretDescription(secret, description);
        if (log.isDebugEnabled()) {
            log.debug(secretId + "secret description updated successfully.");
        }
        return updatedSecret;
    }

    @Override
    public SecretType getSecretType(String secretTypeName) throws SecretManagementException {

        validateSecretType(secretTypeName);

        SecretType secretType = new SecretType();
        secretType.setName(secretTypeName);

        if (log.isDebugEnabled()) {
            log.debug("Secret type: " + secretType.getName() + " retrieved successfully.");
        }
        return secretType;
    }

    /**
     * Validate that secret type and secret name is non-empty.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secretName     The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretRetrieveRequest(String secretTypeName, String secretName) throws SecretManagementException {

        validateSecretType(secretTypeName);

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret identifier with secretName: " + secretName
                        + " and secretTypeName: " + secretName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_GET_REQUEST_INVALID, null);
        }
    }

    private void validateSecretsRetrieveRequest(String secretTypeName)
            throws SecretManagementException {

        if (StringUtils.isEmpty(secretTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret identifier with secretTypeName: " + secretTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_GET_REQUEST_INVALID, null);
        }
    }

    /**
     * Validate that secret type and secret name is non-empty.
     * Set tenant domain if they are not set to the secret object.
     *
     * @param secretTypeName Name of the {@link SecretType}.
     * @param secretName     The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretDeleteRequest(String secretTypeName, String secretName)
            throws SecretManagementException {

        validateSecretType(secretTypeName);

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Error identifying the secret with secret name: " + secretName + " and secret type: "
                        + secretTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_DELETE_REQUEST_REQUIRED, null);
        }

        if (!isSecretExist(secretTypeName, secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the name: " + secretName + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName);
        }
    }

    /**
     * Validate that secret type and secret name and value are non-empty.
     * Set tenant domain if they are not set to the secret object.
     *
     * @param secret The secret to be added.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretCreateRequest(String secretTypeName, Secret secret) throws SecretManagementException {

        validateSecretType(secretTypeName);

        if (StringUtils.isEmpty(secret.getSecretName()) || StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ERROR_CODE_SECRET_ADD_REQUEST_INVALID, null);
        }
        if (isSecretExist(secretTypeName, secret.getSecretName())) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the name: " + secret.getSecretName() + " does exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_ALREADY_EXISTS, secret.getSecretName());
        }

        validateSecretName(secret.getSecretName());
        validateSecretValue(secret.getSecretValue());
        validateSecretDescription(secret.getDescription());

        if (StringUtils.isEmpty(secret.getTenantDomain())) {
            secret.setTenantDomain(getTenantDomain());
        }
    }

    /**
     * Validate that secret type and secret name is non empty. Validate the secret existence.
     * Set tenant domain if it is not set to the secret object.
     *
     * @param secret The secret to be replaced.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretReplaceRequest(String secretTypeName, Secret secret)
            throws SecretManagementException {

        validateSecretType(secretTypeName);

        if (StringUtils.isEmpty(secret.getSecretId()) || StringUtils.isEmpty(secret.getSecretValue())) {
            throw handleClientException(ERROR_CODE_SECRET_REPLACE_REQUEST_INVALID, null);
        }

        if (!isSecretExistById(secret.getSecretId())) {
            if (log.isDebugEnabled()) {
                log.debug("A secret with the id: " + secret.getSecretId() + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secret.getSecretId());
        }

        if (StringUtils.isEmpty(secret.getTenantDomain())) {
            secret.setTenantDomain(getTenantDomain());
        }
    }

    /**
     * Select highest priority Secret DAO from an already sorted list of Secret DAOs.
     *
     * @return Highest priority Secret DAO.
     */
    private SecretDAO getSecretDAO() throws SecretManagementException {

        if (!this.secretDAOS.isEmpty()) {
            return secretDAOS.get(secretDAOS.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, "secretDAOs");
        }
    }

    private void validateSecretManagerEnabled() throws SecretManagementServerException {

        if (!SecretManagerComponentDataHolder.getInstance().isSecretManagementEnabled()) {
            throw handleServerException(ERROR_CODE_SECRET_MANAGER_NOT_ENABLED);
        }
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Check whether a secret already exists with the secret name.
     *
     * @param secretTypeName Type of the secret.
     * @param secretName Name of the secret.
     * @return true if exists.
     * @throws SecretManagementException
     */
    private boolean isSecretExist(String secretTypeName, String secretName) throws SecretManagementException {

        try {
            getSecret(secretTypeName, secretName);
        } catch (SecretManagementClientException e) {
            if (ERROR_CODE_SECRET_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                return false;
            }
            throw e;
        }
        return true;
    }

    /**
     * Check whether a secret already exists with the secret id.
     *
     * @param secretId Id of the secret.
     * @return true if exists.
     * @throws SecretManagementException
     */
    private boolean isSecretExistById(String secretId) throws SecretManagementException {

        try {
            getSecretById(secretId);
        } catch (SecretManagementClientException e) {
            if (ERROR_CODE_SECRET_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                return false;
            }
            throw e;
        }
        return true;
    }

    private String getEncryptedSecret(String secretValue, String name) throws SecretManagementServerException {

        try {
            return encrypt(secretValue);
        } catch (CryptoException e) {
            throw handleServerException(ERROR_CODE_ADD_SECRET, name, e);
        }
    }

    /**
     * Encrypt secret.
     *
     * @param plainText plain text secret.
     * @return encrypted secret.
     */
    private String encrypt(String plainText) throws CryptoException {

        return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                plainText.getBytes(Charsets.UTF_8));
    }

    /**
     * Validate the secret type.
     *
     * @param secretTypeName The secret type name to be retrieved.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretType(String secretTypeName) throws SecretManagementException {

        if (StringUtils.isEmpty(secretTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type name: " + secretTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_TYPE_NAME_REQUIRED, null);
        } else if (!EnumUtils.isValidEnum(SecretConstants.SecretTypes.class, secretTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret type: " + secretTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_TYPE_DOES_NOT_EXISTS, null);
        }
    }

    /**
     * Validate secret name against a regex pattern.
     * @param secretName Name of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretName(String secretName) throws SecretManagementClientException {
        if(!isSecretNameRegexValid(secretName)) {
            if(log.isDebugEnabled()) {
                log.debug("Secret name does not conform to " + getSecretNameRegex() + " pattern");
            }
            throw handleClientException(ERROR_CODE_INVALID_SECRET_NAME, getSecretNameRegex());
        }
    }

    /**
     * Validate secret value against a regex pattern.
     * @param secretValue Value of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretValue(String secretValue) throws SecretManagementClientException {
        if(!isSecretValueRegexValid(secretValue)) {
            if(log.isDebugEnabled()) {
                log.debug("Secret value does not conform to " + getSecretValueRegex() + " pattern");
            }
            throw handleClientException(ERROR_CODE_INVALID_SECRET_VALUE, getSecretValueRegex());
        }
    }

    /**
     * Validate secret description against a regex pattern.
     * @param description Description of the secret.
     * @throws SecretManagementClientException
     */
    private void validateSecretDescription(String description) throws SecretManagementClientException {
        if(description != null && !isSecretDescriptionRegexValid(description)) {
            if(log.isDebugEnabled()) {
                log.debug("Secret description does not conform to "
                        + getSecretDescriptionRegex() + " pattern");
            }
            throw handleClientException(ERROR_CODE_INVALID_SECRET_DESCRIPTION,
                    getSecretDescriptionRegex());
        }
    }
}
