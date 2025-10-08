package io.onedev.server.util.facade;

import org.jspecify.annotations.Nullable;

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
