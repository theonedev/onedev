package com.pmease.commons.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/dummy")
public class DummyResource {

	@GET
	@Produces("text/plain")
	public String get() {
		return "Dummy's gold.";
	}
	
}
