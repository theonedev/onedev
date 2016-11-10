package com.gitplex.commons.shiro;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.WebSecurityManager;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gitplex.commons.hibernate.AbstractEntity;
import com.gitplex.commons.loader.AppLoader;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AbstractUser extends AbstractEntity implements AuthenticationInfo {

	public static final String HASH_PREFIX = "@hash^prefix@";
	
    @Column(unique=true, nullable=false)
    private String name;

    @Column(length=1024)
    @JsonDeserialize(using=PasswordDeserializer.class)
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get hashed password of this user.
     *  
     * @return
     * 			hashed password of this user
     */
    public String getPassword() {
    	return password;
    }
    
    /**
     * Set plain text password of this user. 
     * <p>
     * The plain text password will be hashed via {@link PasswordService} before stored. Bind PasswordService to 
     * your own implementation via Guice if you'd like to change the default hash algorithm.
     * 
     * @param password
     * 			plain text password to set
     */
    public void setPassword(String password) {
    	if (password != null) {
    		if (password.startsWith(HASH_PREFIX))
    			this.password = password;
    		else
    			this.password = HASH_PREFIX + AppLoader.getInstance(PasswordService.class).encryptPassword(password);
    	} else {
    		this.password = "";
    	}
    }
    
    @Override
    public PrincipalCollection getPrincipals() {
        return new SimplePrincipalCollection(getId(), "");
    }
    
    @Override
    public Object getCredentials() {
    	if (password != null && password.startsWith(HASH_PREFIX))
    		return password.substring(HASH_PREFIX.length());
    	else
    		return password;
    }

    public Subject asSubject() {
    	return asSubject(getId());
    }

    public static Long getCurrentId() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        Preconditions.checkNotNull(principal);
        return (Long) principal;
    }

    public static PrincipalCollection asPrincipal(Long userId) {
        return new SimplePrincipalCollection(userId, "");
    }
    
    public static Subject asSubject(Long userId) {
    	WebSecurityManager securityManager = AppLoader.getInstance(WebSecurityManager.class);
        return new Subject.Builder(securityManager).principals(asPrincipal(userId)).buildSubject();
    }
    
}
