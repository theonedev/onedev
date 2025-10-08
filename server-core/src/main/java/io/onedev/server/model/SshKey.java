package io.onedev.server.model;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotEmpty;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.validation.Validatable;

@Editable
@Entity
@Table(indexes={@Index(columnList="o_owner_id"), @Index(columnList="fingerprint")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@ClassValidating
public class SshKey extends AbstractEntity implements Validatable {
    
    private static final long serialVersionUID = 1L;
    
    @Column(nullable=false, length=5000)
    private String content;
    
    @JsonIgnore
    @Column(nullable=false, unique=true)
    private String fingerprint;

    @JsonIgnore
    @Column(nullable=false)
    private Date createdAt;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    @Immutable
    private User owner;

    @Editable(name="OpenSSH Public Key", placeholder="OpenSSH public key begins with 'ssh-rsa', "
    		+ "'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', 'ecdsa-sha2-nistp521', 'ssh-ed25519', "
    		+ "'sk-ecdsa-sha2-nistp256@openssh.com', or 'sk-ssh-ed25519@openssh.com'")
    @NotEmpty
    @Multiline
    @OmitName
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
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
    
    public void generateFingerprint() {
        try {
            PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(content);
            fingerprint = KeyUtils.getFingerPrint(BuiltinDigests.sha256, pubEntry);
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
