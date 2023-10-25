package io.onedev.server.util.facade;

public class PackFacade extends ProjectBelongingFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	public PackFacade(Long id, Long projectId, String name) {
		super(id, projectId);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
