package com.pmease.commons.shiro;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractUser extends AbstractEntity implements AuthenticationInfo {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 1024)
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
    
    public void setPassword(String password) {
        passwordHash = AppLoader.getInstance(PasswordService.class)
                .encryptPassword(password);
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
    	WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
        return new Subject.Builder(securityManager).principals(asPrincipal(getId())).buildSubject();
    }

    public static Long getCurrentId() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        Preconditions.checkNotNull(principal);
        return (Long) principal;
    }

    public static PrincipalCollection asPrincipal(Long userId) {
        return new SimplePrincipalCollection(userId, "");
    }
}
