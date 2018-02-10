package com.turbodev.server.util.facade;

import com.turbodev.server.model.Membership;

public class MembershipFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long groupId;
	
	private final Long userId;
	
	public MembershipFacade(Membership membership) {
		super(membership.getId());
		
		groupId = membership.getGroup().getId();
		userId = membership.getUser().getId();
	}

	public Long getGroupId() {
		return groupId;
	}

	public Long getUserId() {
		return userId;
	}
	
}
