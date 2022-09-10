package io.onedev.server.util.facade;

public class GroupFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	public GroupFacade(Long id, String name) {
		super(id);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
