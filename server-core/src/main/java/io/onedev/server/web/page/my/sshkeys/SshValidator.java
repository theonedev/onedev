package io.onedev.server.web.page.my.sshkeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import io.onedev.server.git.ssh.SshKeyUtils;
import io.onedev.server.model.SshKey;

public class SshValidator implements IValidator<String> {

    private IModel<SshKey> formModel;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SshValidator(IModel<SshKey> formModel) {
        super();
        this.formModel = formModel;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        try {
            PublicKey pubEntry = SshKeyUtils.loadPublicKey(validatable.getValue());
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
            
            formModel.getObject().setDigest(fingerPrint);
            
        } catch (IOException | GeneralSecurityException e) {
            
        }
    }

}
