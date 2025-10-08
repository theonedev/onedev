package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.AccessTokenFacade;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import org.jspecify.annotations.Nullable;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.security.SecurityUtils.asAccessTokenPrincipal;
import static io.onedev.server.security.SecurityUtils.asPrincipals;

@Entity
@Table(
		indexes={@Index(columnList="o_owner_id"), @Index(columnList = "value")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_owner_id", "name"})})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class AccessToken extends AbstractEntity implements AuthenticationInfo {

	private static final long serialVersionUID = 1L;
	
	@Column(nullable=false)
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User owner;

	@Column(nullable=false, unique=true)
	@JsonProperty(access = READ_ONLY)
	private String value = CryptoUtils.generateSecret();
	
	private boolean hasOwnerPermissions;

	@JsonProperty(access = READ_ONLY)
	private Date createDate = new Date();

	private Date expireDate;

	@OneToMany(mappedBy="token", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<AccessTokenAuthorization> authorizations = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isHasOwnerPermissions() {
		return hasOwnerPermissions;
	}

	public void setHasOwnerPermissions(boolean hasOwnerPermissions) {
		this.hasOwnerPermissions = hasOwnerPermissions;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Nullable
	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(@Nullable Date expireDate) {
		this.expireDate = expireDate;
	}

	public boolean isExpired() {
		return getExpireDate() != null && getExpireDate().before(new Date());
	}
	
	public Collection<AccessTokenAuthorization> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(Collection<AccessTokenAuthorization> authorizations) {
		this.authorizations = authorizations;
	}

	public Subject asSubject() {
		// Temporal access token, check AccessTokenService.createTemporal
		if (isNew() || isHasOwnerPermissions())
			return getOwner().asSubject();
		else
			return SecurityUtils.asSubject(getPrincipals());
	}

	public AccessTokenFacade getFacade() {
		return new AccessTokenFacade(getId(), getOwner().getId(), getName(), getValue(), getExpireDate());
	}

	@Override
	public PrincipalCollection getPrincipals() {
		return asPrincipals(asAccessTokenPrincipal(getId()));
	}

	@Override
	public Object getCredentials() {
		throw new UnsupportedOperationException();
	}
	
}
