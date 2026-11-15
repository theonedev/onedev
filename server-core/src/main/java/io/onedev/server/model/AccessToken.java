package io.onedev.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.rest.annotation.Immutable;
import io.onedev.server.security.SecurityUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import org.jspecify.annotations.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static io.onedev.server.model.AccessToken.PROP_VALUE_HASH;
import static io.onedev.server.security.SecurityUtils.asAccessTokenPrincipal;
import static io.onedev.server.security.SecurityUtils.asPrincipals;

@Entity
@Table(indexes={
		@Index(columnList="owner_id"),
		@Index(columnList=PROP_VALUE_HASH, unique=true),
		@Index(columnList="oauthGrant_id")
})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class AccessToken extends AbstractEntity implements AuthenticationInfo {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String PROP_VALUE_HASH = "valueHash";
	
	@Column(nullable=false)
	@NotEmpty
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	@Immutable
	private User owner;

	@Column(nullable=false)
	@JsonIgnore
	private String valueHash;
	
	private boolean hasOwnerPermissions;

	@JsonProperty(access = READ_ONLY)
	private Date createDate = new Date();

	private Date expireDate;

	@ManyToOne(fetch=FetchType.LAZY)
	@JsonIgnore
	private OAuthGrant oauthGrant;

	public OAuthGrant getOauthGrant() {
		return oauthGrant;
	}

	public void setOauthGrant(OAuthGrant oauthGrant) {
		this.oauthGrant = oauthGrant;
	}

	@OneToMany(mappedBy="token", cascade=CascadeType.REMOVE)
	@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
	private Collection<AccessTokenAuthorization> authorizations = new ArrayList<>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public String generate() {
		var value = RandomStringUtils.secure().nextAlphanumeric(40);
		valueHash = DigestUtils.sha256Hex(value);
		return value;
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
		// isNew() checks temporal access token, check AccessTokenService.createTemporal
		if (oauthGrant == null && (isNew() || isHasOwnerPermissions()))
			return getOwner().asSubject();
		else
			return SecurityUtils.asSubject(getPrincipals());
	}

	public String toAuditXml() {
		return toAuditXml(VersionedXmlDoc.fromBean(this));
	}

	public static String toAuditXml(VersionedXmlDoc snapshot) {
		snapshot.getRootElement().element(PROP_VALUE_HASH).detach();
		return snapshot.toXML();
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
