package io.onedev.server.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.onedev.server.web.editable.annotation.Editable;

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
    private String verificationCode = RandomStringUtils.randomAlphanumeric(16);
    
    private boolean primary;
    
    private boolean git;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(nullable=false)
    private User owner;

    @OneToMany(mappedBy="emailAddress", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
    private Collection<GpgKey> gpgKeys = new ArrayList<>();
    
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
	public String getVerficationCode() {
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

	public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Collection<GpgKey> getGpgKeys() {
		return gpgKeys;
	}

	public void setGpgKeys(Collection<GpgKey> gpgKeys) {
		this.gpgKeys = gpgKeys;
	}

	public boolean isVerified() {
    	return getVerficationCode() == null;
    }

}
