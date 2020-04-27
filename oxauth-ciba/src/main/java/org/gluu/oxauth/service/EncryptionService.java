/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.service;

import org.gluu.util.StringHelper;
import org.gluu.util.security.PropertiesDecrypter;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

/**
 * Allows to encrypt/decrypt strings using a pre-configured key from oxCore.
 *
 * @author Milton BO Date: 27/04/2020
 */
@ApplicationScoped
@Named
public class EncryptionService {

    @Inject
    private Logger log;

    @Inject
    private StringEncrypter stringEncrypter;

    public String decrypt(String encryptedString) throws EncryptionException {
		if (StringHelper.isEmpty(encryptedString)) {
			return null;
		}

		return stringEncrypter.decrypt(encryptedString);
    }

	public String decrypt(String encryptedValue, boolean returnSource) {
		if (encryptedValue == null) {
			return encryptedValue;
		}

		String resultValue;
		if (returnSource) {
			resultValue = encryptedValue;
		} else {
			resultValue = null;
		}

		try {
			resultValue = stringEncrypter.decrypt(encryptedValue);
		} catch (Exception ex) {
			if (!returnSource) {
				log.error(String.format("Failed to decrypt value: '%s'", encryptedValue, ex));
			}
		}

		return resultValue;
	}

	public String encrypt(String unencryptedString) throws EncryptionException {
		if (StringHelper.isEmpty(unencryptedString)) {
			return null;
		}

		return stringEncrypter.encrypt(unencryptedString);
	}

	public Properties decryptProperties(Properties connectionProperties) {
		return PropertiesDecrypter.decryptProperties(stringEncrypter, connectionProperties);
	}

    public Properties decryptAllProperties(Properties connectionProperties) {
        return PropertiesDecrypter.decryptAllProperties(stringEncrypter, connectionProperties);
    }

}