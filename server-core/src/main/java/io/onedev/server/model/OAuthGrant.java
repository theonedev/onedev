package io.onedev.server.model;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(indexes={@Index(columnList="application_id"), @Index(columnList="owner_id"),
		@Index(columnList="expireDate"), @Index(columnList="lastUsedDate,codeExpireDate")})
public class OAuthGrant extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private OAuthApplication application;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false, length=4096)
	private String scopes;

	@Column(nullable=false, length=2048)
	private String redirectUri;

	private String codeHash;

	private String codeChallenge;

	private Date codeExpireDate;

	private String nonce;

	private String refreshTokenHash;

	@Lob
	@Column(length=1048576)
	private ArrayList<String> usedRefreshTokenHashes = new ArrayList<>();

	@Column(nullable=false)
	private Date createDate = new Date();

	@Column(nullable=false)
	private Date expireDate;

	private Date lastUsedDate;

	@OneToMany(mappedBy="oauthGrant", cascade=CascadeType.REMOVE)
	private Collection<AccessToken> accessTokens = new ArrayList<>();

	public OAuthApplication getApplication() {
		return application;
	}

	public void setApplication(OAuthApplication application) {
		this.application = application;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getCodeHash() {
		return codeHash;
	}

	public void setCodeHash(String codeHash) {
		this.codeHash = codeHash;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public void setCodeChallenge(String codeChallenge) {
		this.codeChallenge = codeChallenge;
	}

	public Date getCodeExpireDate() {
		return codeExpireDate;
	}

	public void setCodeExpireDate(Date codeExpireDate) {
		this.codeExpireDate = codeExpireDate;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getRefreshTokenHash() {
		return refreshTokenHash;
	}

	public void setRefreshTokenHash(String refreshTokenHash) {
		this.refreshTokenHash = refreshTokenHash;
	}

	public ArrayList<String> getUsedRefreshTokenHashes() {
		return usedRefreshTokenHashes;
	}

	public void setUsedRefreshTokenHashes(ArrayList<String> usedRefreshTokenHashes) {
		this.usedRefreshTokenHashes = usedRefreshTokenHashes;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}

	public Collection<AccessToken> getAccessTokens() {
		return accessTokens;
	}

	public void setAccessTokens(Collection<AccessToken> accessTokens) {
		this.accessTokens = accessTokens;
	}

	public boolean isActive() {
		return !isExpired() && !owner.isDisabled() && !application.getOwner().isDisabled();
	}

	public boolean isExpired() {
		var now = new Date();
		return !expireDate.after(now) || lastUsedDate == null && !codeExpireDate.after(now);
	}

}
