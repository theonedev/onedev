package com.gitplex.server.util.facade;

public class MembershipFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long groupId;
	
	private final Long userId;
	
	public MembershipFacade(Long id, Long groupId, Long userId) {
		super(id);
		
		this.groupId = groupId;
		this.userId = userId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public Long getUserId() {
		return userId;
	}
	
}
