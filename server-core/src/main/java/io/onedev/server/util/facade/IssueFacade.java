package io.onedev.server.util.facade;

public class IssueFacade extends ProjectBelongingFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final Long number;
	
	public IssueFacade(Long id, Long projectId, Long number) {
		super(id, projectId);
		this.number = number;
	}

	public Long getNumber() {
		return number;
	}

}