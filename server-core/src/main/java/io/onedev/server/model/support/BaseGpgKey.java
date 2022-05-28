package io.onedev.server.model.support;

import java.util.List;
import java.util.stream.Collectors;

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
    
    private transient List<PGPPublicKey> publicKeys;

    @Editable(name="GPG Public Key", placeholder="GPG public key begins with '-----BEGIN PGP PUBLIC KEY BLOCK-----'")
    @NotEmpty
    @Multiline
    @OmitName
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public List<PGPPublicKey> getPublicKeys() {
		if (publicKeys == null)
			publicKeys = GpgUtils.parse(content);
		return publicKeys;
	}
	
	public List<Long> getKeyIds() {
		return getPublicKeys().stream().map(it->it.getKeyID()).collect(Collectors.toList());
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
