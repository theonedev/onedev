package com.gitplex.server.web;

import java.util.Set;

import com.gitplex.server.core.util.validation.TeamNameReservation;
import com.google.common.collect.Sets;

public class WebTeamNameReservation implements TeamNameReservation {

	@Override
	public Set<String> getReserved() {
		return Sets.newHashSet("new"); // reserved for url segment when creating new team
	}

}
