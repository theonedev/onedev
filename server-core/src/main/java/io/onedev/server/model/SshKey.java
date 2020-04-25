package io.onedev.server.model;

import java.security.PublicKey;
import java.time.LocalDateTime;

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
import org.apache.wicket.util.string.Strings;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;

@Editable
@ClassValidating
@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList="name"), @Index(columnList="digest")},
		uniqueConstraints={
				@UniqueConstraint(columnNames={"o_owner_id", "name"}), 
				@UniqueConstraint(columnNames={"digest"})}
)
public class SshKey extends AbstractEntity implements Validatable {
    
    private static final long serialVersionUID = 1L;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, length = 5000)
    private String content;
    
    @Column(nullable=false)
    private String digest;
    
    @Column(nullable=false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User owner;

    @Editable(name = "Name")
    @NotEmpty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Editable(name = "Key Value")
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean isValid(ConstraintValidatorContext context) {
        String propertyNode = "content";
        boolean hasErrors = false;
        String errorMessage = "";
        
        if (Strings.isEmpty(content)) {
            return false;
        }
        
        try {
            SshKeyManager sshKeyManager = OneDev.getInstance(SshKeyManager.class);
            PublicKey pubEntry = SshKeyUtils.decodeSshPublicKey(content);
            String fingerPrint = KeyUtils.getFingerPrint(SshKeyUtils.MD5_DIGESTER, pubEntry);
            
            boolean alreadyInUse = sshKeyManager.findByDigest(fingerPrint) != null;
            
            if (alreadyInUse) {
                errorMessage = "The provided key is already in use. Please use another one.";
                hasErrors  = true;
            } 
            
        } catch (Exception exception) {
            errorMessage = "The value provided as key is invalid. Please checkit and try again.";
            hasErrors = true;
        } 
        
        if (hasErrors) {            
            context.buildConstraintViolationWithTemplate(errorMessage)
                .addPropertyNode(propertyNode).addConstraintViolation()
                .disableDefaultConstraintViolation();
            return false;
        }
        return true;
    }
}
