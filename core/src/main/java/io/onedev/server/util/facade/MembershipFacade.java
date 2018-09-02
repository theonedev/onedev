package io.onedev.server.util.facade;

import io.onedev.server.model.Membership;

public class MembershipFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final Long teamId;
	
	private final Long userId;
	
	public MembershipFacade(Membership membership) {
		super(membership.getId());
		
		teamId = membership.getTeam().getId();
		userId = membership.getUser().getId();
	}

	public Long getTeamId() {
		return teamId;
	}

	public Long getUserId() {
		return userId;
	}
	
}
