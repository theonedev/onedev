package com.pmease.commons.product;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.shiro.SecurityUtils;

@Path("/whoami")
public class HelloResource {
	
	@GET
	public String whoami() {
		SecurityUtils.getSubject().checkPermission("admin");
		return "user: " + SecurityUtils.getSubject().getPrincipal();
	}
	
}
