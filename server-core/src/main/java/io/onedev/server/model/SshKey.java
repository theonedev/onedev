package io.onedev.server.model;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.ConstraintValidatorContext;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.security.CipherUtils;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable
@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList="digest")},
		uniqueConstraints={@UniqueConstraint(columnNames={"digest"})}
)
@ClassValidating
public class SshKey extends AbstractEntity implements Validatable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(nullable=false, length=5000)
    private String content;
    
    @JsonIgnore
    @Column(nullable=false)
    private String digest;

    @JsonIgnore
    @Column(nullable=false)
    private Date createdAt;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User owner;

    @Editable(name="OpenSSH Public Key", description="Provide a OpenSSH public key. Normally begins with 'ssh-rsa'")
    @NotEmpty
    @Multiline
    @OmitName
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Nullable
    public String getComment() {
    	String tempStr = StringUtils.substringAfter(content, " ");
    	if (tempStr.indexOf(' ') != -1)
    		return StringUtils.substringAfter(tempStr, " ");
    	else
    		return null;
    }
    
    public void digest() {
        try {
            PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(content);
            digest = KeyUtils.getFingerPrint(CipherUtils.DIGEST_FORMAT, pubEntry);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (content == null) {
			return true;
		} else {
	        try {
	            SshKeyUtils.decodeSshPublicKey(content);
	            return true;
	        } catch (Exception exception) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Invalid OpenSSH public key")
						.addPropertyNode("content").addConstraintViolation();
				return false;
	        } 
		}
	}

}
