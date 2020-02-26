package io.onedev.server.web.page.my.sshkeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.git.ssh.SshKeyUtils;
import io.onedev.server.model.SshKey;

public class SshValidator implements IValidator<String> {

    private IModel<SshKey> formModel;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SshValidator(IModel<SshKey> formModel) {
        this.formModel = formModel;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        try {
            SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
            PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(validatable.getValue());
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
            
            boolean alreadyInUse = sshKeyManager.isKeyAlreadyInUse(fingerPrint);
            
            if (alreadyInUse) {
                IValidationError keyAlreadyInUse = new ValidationError("The key you want to add is already in use.");
                validatable.error(keyAlreadyInUse);
                
                return;
            }
            
            formModel.getObject().setDigest(fingerPrint);
            
        } catch (IOException | GeneralSecurityException exception) {
            IValidationError badContent = new ValidationError("The value provided as key is invalid. Please checkit and try again");
            validatable.error(badContent);
        } 
    }

}
