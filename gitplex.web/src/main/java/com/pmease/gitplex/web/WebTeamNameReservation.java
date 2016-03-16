package com.pmease.gitplex.web;

import java.util.Set;

import com.google.common.collect.Sets;
import com.pmease.gitplex.core.util.validation.TeamNameReservation;

public class WebTeamNameReservation implements TeamNameReservation {

	@Override
	public Set<String> getReserved() {
		return Sets.newHashSet("new"); // reserved for url segment when creating new team
	}

}
