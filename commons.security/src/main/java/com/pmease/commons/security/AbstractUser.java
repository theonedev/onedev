package com.pmease.commons.security;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@MappedSuperclass
public class AbstractUser extends AbstractEntity implements AuthenticationInfo {

	@Column(unique=true, nullable=false)
	private String name;
	
	@Column(length=1024)
	private String passwordHash;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	@Override
	public PrincipalCollection getPrincipals() {
		return new SimplePrincipalCollection(getId(), "");
	}

	@Override
	public Object getCredentials() {
		return passwordHash;
	}

}
