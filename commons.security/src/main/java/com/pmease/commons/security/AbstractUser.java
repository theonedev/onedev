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
	private String loginName;
	
	private String fullName;

	@Column(length=1024)
	private String passwordHash;
	
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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
