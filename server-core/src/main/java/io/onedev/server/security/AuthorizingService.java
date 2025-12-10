package io.onedev.server.security;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

public interface AuthorizingService {
    
    AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals);

}
