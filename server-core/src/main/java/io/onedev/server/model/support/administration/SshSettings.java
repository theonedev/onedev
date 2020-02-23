package io.onedev.server.model.support.administration;

import java.io.Serializable;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraints.NotEmpty;
import io.onedev.server.git.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable
@ClassValidating
public class SshSettings implements Serializable, Validatable {

    private static final long serialVersionUID = 774556852129893390L;

    private String serverSshUrl;

    private String privateKey;

    private String publicKey;

    @Editable(name = "Ssh URL", order = 90,
            description = "Specify the URL to use with Git to access repositories via SSH")
    @NotEmpty
    public String getServerSshUrl() {
        return serverSshUrl;
    }

    public void setServerSshUrl(String serverSshUrl) {
        this.serverSshUrl = serverSshUrl;
    }

    @Editable(name = "Server private Key", order = 90,
            description = "Specify the private key (in PEM format) used by SSH server to establish connections")
    @Multiline
    @NotEmpty
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Editable(name = "Server public Key", order = 90,
            description = "Specify the public key (in PEM format) used by SSH server to establish connections")
    @Multiline
    @NotEmpty
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public boolean isValid(ConstraintValidatorContext context) {
        boolean hasErrors = false;
        String propertyNode = "privateKey";
        try {
            SshKeyUtils.decodePrivateKey(privateKey);
            propertyNode = "publicKey";
            SshKeyUtils.decodePublicKey(publicKey);
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("The provided key is not valid. Please check it and try again.")
                    .addPropertyNode(propertyNode).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            hasErrors = true;
        }
        return !hasErrors;
    }
}
