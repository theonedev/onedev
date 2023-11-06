package io.onedev.server.plugin.pack.container;

import io.onedev.server.pack.PackSupport;

import javax.inject.Singleton;

@Singleton
public class ContainerPackSupport implements PackSupport {

	public static final String TYPE = "Container Image";

	@Override
	public String getPackType() {
		return TYPE;
	}

}
