package io.onedev.server.model;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(indexes=@Index(columnList="owner_id"))
public class OAuthApplication extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Column(nullable=false)
	private String name;

	@Column(nullable=false, length=2048)
	private String homepage;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User owner;

	@Column(nullable=false, unique=true)
	private String clientId;

	private String secretHash;

	private boolean confidential;

	@Column(nullable=false, length=65535)
	private String redirectUris;

	@OneToMany(mappedBy="application", cascade=CascadeType.REMOVE)
	private Collection<OAuthGrant> grants = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecretHash() {
		return secretHash;
	}

	public void setSecretHash(String secretHash) {
		this.secretHash = secretHash;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	public Collection<OAuthGrant> getGrants() {
		return grants;
	}

	public void setGrants(Collection<OAuthGrant> grants) {
		this.grants = grants;
	}

	public List<String> getRedirectUriList() {
		return redirectUris.lines().map(String::trim).filter(it -> !it.isEmpty()).toList();
	}

}
