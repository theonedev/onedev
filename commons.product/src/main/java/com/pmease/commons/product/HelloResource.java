package com.pmease.commons.product;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

@Path("/whoami")
public class HelloResource {
	
	@GET
	@RequiresPermissions("view")
	public String whoami() {
		return "user: " + SecurityUtils.getSubject().getPrincipal();
	}
	
}
