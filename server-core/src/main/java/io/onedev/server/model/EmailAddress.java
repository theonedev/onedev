package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.EmailAddressFacade;
import io.onedev.server.annotation.Editable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Editable
@Entity
@Table(indexes={@Index(columnList="o_owner_id"), @Index(columnList="value")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class EmailAddress extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    public static final String PROP_OWNER = "owner";
    
    public static final String PROP_VALUE = "value";
    
    @Column(nullable=false, unique=true)
    private String value;
    
    @JsonIgnore
    private String verificationCode = CryptoUtils.generateSecret();
    
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean primary;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean git;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private boolean open;
    	
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
	@Immutable
    private User owner;

    @Editable
    @Email
    @NotEmpty
    public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Editable
	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	@Editable
	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	@Editable
	public boolean isGit() {
		return git;
	}

	public void setGit(boolean git) {
		this.git = git;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

	public boolean isVerified() {
    	return getVerificationCode() == null;
    }

	public EmailAddressFacade getFacade() {
		return new EmailAddressFacade(getId(), getOwner().getId(), getValue(), 
				isPrimary(), isGit(), isOpen(), getVerificationCode());
	}
	
}
