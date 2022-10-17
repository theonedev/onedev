package io.onedev.server.model.support.administration;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.validation.ConstraintValidatorContext;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import javax.validation.constraints.NotEmpty;

import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
@ClassValidating
public class SshSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
    private String pemPrivateKey;
    
    @Editable(placeholder="PEM private key begins with '-----BEGIN RSA PRIVATE KEY-----'")
    @OmitName
    @Multiline
    @NotEmpty
    public String getPemPrivateKey() {
        return pemPrivateKey;
    }

    public void setPemPrivateKey(String pemPrivateKey) {
        this.pemPrivateKey = pemPrivateKey;
    }
    
    public String getFingerPrint() {
        try {
			PrivateKey privateKey = SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
			PublicKey publicKey = KeyUtils.recoverPublicKey(privateKey);
			return KeyUtils.getFingerPrint(BuiltinDigests.sha256, publicKey);
		} catch (IOException | GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
    }
    
    public PrivateKey getPrivateKey() {
		try {
			return SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
		} catch (IOException | GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
    }
    
    @Override
    public boolean isValid(ConstraintValidatorContext context) {
        boolean hasErrors = false;
        String propertyNode = "pemPrivateKey";
        try {
            SshKeyUtils.decodePEMPrivateKey(pemPrivateKey);
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("The provided key is not valid. Please check and try again")
                    .addPropertyNode(propertyNode).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            hasErrors = true;
        }
        return !hasErrors;
    }
}
