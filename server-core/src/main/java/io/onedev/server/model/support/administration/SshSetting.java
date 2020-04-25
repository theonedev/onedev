package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable
@ClassValidating
public class SshSetting implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

    private String serverUrl;

    private String privateKey;
    
	@Editable(name="SSH Server URL", order=90, description="This property will be used as base to construct "
			+ "urls of various ssh services such as git over ssh")
	@NotEmpty
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Editable(name="Server Private Key", order=100, description="Specify the private key (in PEM format) used "
    		+ "by ssh server to establish connections with ssh client")
    @Multiline
    @NotEmpty
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    
    @Override
    public boolean isValid(ConstraintValidatorContext context) {
		if (serverUrl != null)
			serverUrl = StringUtils.stripEnd(serverUrl, "/\\");
    	
        boolean hasErrors = false;
        String propertyNode = "privateKey";
        try {
            SshKeyUtils.decodePEMPrivateKey(privateKey);
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("The provided key is not valid. Please check and try again")
                    .addPropertyNode(propertyNode).addConstraintViolation()
                    .disableDefaultConstraintViolation();
            hasErrors = true;
        }
        return !hasErrors;
    }
}
