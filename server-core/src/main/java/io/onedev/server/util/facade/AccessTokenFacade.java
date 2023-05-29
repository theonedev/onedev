package io.onedev.server.util.facade;

public class AccessTokenFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final Long ownerId;

	private final String value;
	
	public AccessTokenFacade(Long id, Long ownerId, String value) {
		super(id);
		this.ownerId = ownerId;
		this.value = value;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public String getValue() {
		return value;
	}
	
}
