package io.onedev.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.onedev.server.annotation.Editable;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.EmailAddressFacade;

@Editable
@Entity
@Table(indexes={@Index(columnList="owner_id"), @Index(columnList="value")})
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
				isPrimary(), getVerificationCode());
	}
		
}
