package io.onedev.server.util.facade;

import javax.annotation.Nullable;

import io.onedev.server.model.LinkSpec;

public class LinkSpecFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String oppositeName;
	
	public LinkSpecFacade(Long id, String name, @Nullable String oppositeName) {
		super(id);
		this.name = name;
		this.oppositeName = oppositeName;
	}

	public String getName() {
		return name;
	}

	public String getOppositeName() {
		return oppositeName;
	}

}
