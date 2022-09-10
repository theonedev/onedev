package io.onedev.server.util.facade;

public class RoleFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	public RoleFacade(Long id, String name) {
		super(id);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
