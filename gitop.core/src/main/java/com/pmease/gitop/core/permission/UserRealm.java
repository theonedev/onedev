package com.pmease.gitop.core.permission;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.pmease.commons.shiro.AbstractRealm;
import com.pmease.commons.shiro.AbstractUser;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

@Singleton
public class UserRealm extends AbstractRealm {

    private final UserManager userManager;

    @Inject
    public UserRealm(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected AbstractUser getUserByName(String userName) {
        return userManager.find(userName);
    }

    @Override
    protected Collection<Permission> permissionsOf(Long userId) {
        User user;
        if (userId != 0L)
            user = userManager.load(userId);
        else
            user = null;
        
        return userManager.permissionsOf(user);
    }

}
