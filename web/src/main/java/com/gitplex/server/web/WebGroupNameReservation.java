package com.gitplex.server.web;

import java.util.Set;

import com.gitplex.server.util.validation.GroupNameReservation;
import com.google.common.collect.Sets;

public class WebGroupNameReservation implements GroupNameReservation {

	@Override
	public Set<String> getReserved() {
		return Sets.newHashSet("new"); // reserved for url segment when creating new group
	}

}
