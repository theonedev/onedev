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
import io.onedev.server.git.ssh.SshKeyUtils;
import io.onedev.server.model.SshKey;
import io.onedev.server.persistence.dao.Dao;

public class SshValidator implements IValidator<String> {

    private IModel<SshKey> formModel;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Dao dao;

    public SshValidator(IModel<SshKey> formModel, Dao dao) {
        super();
        this.formModel = formModel;
        this.dao = dao;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        try {
            PublicKey pubEntry = SshKeyUtils.decodePublicKey(validatable.getValue());
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
            
            boolean alreadyInUse = SshKeyUtils.isKeyAlreadyInUse(fingerPrint, dao);
            
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
