package com.pmease.gitop.core;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.pmease.commons.security.SecurityHelper;
import com.pmease.gitop.core.model.User;

@Path("/hello")
public class HelloResource {
	
	@GET
	@RequiresPermissions("write")
	public String whoami() {
		return "Hello " + SecurityHelper.getUserDisplayName(User.class, "Guest");
	}
	
}
