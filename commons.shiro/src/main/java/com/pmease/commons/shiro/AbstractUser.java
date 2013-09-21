package com.pmease.commons.shiro;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractUser extends AbstractEntity implements AuthenticationInfo, Permission {

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

	public Subject asSubject() {
		PrincipalCollection principals = new SimplePrincipalCollection(getId(), "");
		return new Subject.Builder().principals(principals).buildSubject();		
	}

	public static AbstractUser getCurrent() {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		Preconditions.checkNotNull(principal);
		Long userId = (Long) principal;
		if (userId != 0L)
			return AppLoader.getInstance(AbstractRealm.class).getUserById(userId);
		else
			return null;
	}
	
}
