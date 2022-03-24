package io.onedev.server.model.support;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.ConstraintValidatorContext;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
@ClassValidating
@MappedSuperclass
public class BaseGpgKey extends AbstractEntity implements Validatable {
    
    private static final long serialVersionUID = 1L;
    
    public static final String PROP_CONTENT = "content";
    
    @Column(nullable=false, length=5000)
    private String content;
    
    private transient PGPPublicKey publicKey;

    @Editable(name="GPG Public Key", description="Provide a GPG public key. Begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'")
    @NotEmpty
    @Multiline
    @OmitName
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public PGPPublicKey getPublicKey() {
		if (publicKey == null)
			publicKey = GpgUtils.parse(content);
		return publicKey;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (content == null) {
			return true;
		} else {
			try {
				GpgUtils.parse(content);
				return true;
			} catch (Exception e) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Invalid GPG Public key")
						.addPropertyNode(PROP_CONTENT).addConstraintViolation();
				return false;
			}
		}
	}
	
}
