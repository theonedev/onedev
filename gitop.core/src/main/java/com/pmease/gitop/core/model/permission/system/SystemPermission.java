package com.pmease.gitop.core.model.permission.system;

import java.io.Serializable;

import org.apache.shiro.authz.Permission;

/**
 * Mark interface to identify system level permissions. 
 * <p>
 * System level permissions are used to define user roles. 
 * 
 * @author robin
 *
 */
public interface SystemPermission extends Permission, Serializable {

}
