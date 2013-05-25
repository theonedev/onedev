package com.pmease.commons.product;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/helloguice")
public class HelloGuice {

	private final GuicyInterface gi;
	
	@Inject
	public HelloGuice(GuicyInterface gi) {
		this.gi = gi;
	}

	@GET
	@Produces("application/json")
	public String get() {
		return gi.get();
	}
	
}
