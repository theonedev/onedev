package io.onedev.server.model;

import java.util.Date;

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

import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable
@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList="name"), @Index(columnList="digest")},
		uniqueConstraints={
				@UniqueConstraint(columnNames={"o_owner_id", "name"}), 
				@UniqueConstraint(columnNames={"digest"})}
)
@ClassValidating
public class SshKey extends AbstractEntity implements Validatable {
    
    private static final long serialVersionUID = 1L;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, length = 5000)
    private String content;
    
    @Column(nullable=false)
    private String digest;
    
    @Column(nullable=false)
    private Date createdAt;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User owner;

    @Editable(description="Specify a name to identify the key")
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Editable(name = "Key", description="Provide a SSH public key. Begins with 'ssh-rsa', 'ssh-ed25519', "
    		+ "'ecdsa-sha2-nistp256', 'ecdsa-sha2-nistp384', or 'ecdsa-sha2-nistp521'")
    @NotEmpty
    @Multiline
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

    public void setDate(Date createdAt) {
        this.createdAt = createdAt;
    }
    
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (content == null) {
			return true;
		} else {
	        try {
	            KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, SshKeyUtils.decodeSshPublicKey(content));
	            return true;
	        } catch (Exception exception) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Invalid SSH public key")
						.addPropertyNode("content").addConstraintViolation();
				return false;
	        } 
		}
	}

}
