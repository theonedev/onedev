package com.gitplex.web;

import java.util.Set;

import com.gitplex.core.util.validation.DepotNameReservation;
import com.google.common.collect.Sets;

public class WebDepotNameReservation implements DepotNameReservation {

	@Override
	public Set<String> getReserved() {
		return Sets.newHashSet("new"); // reserved for url segment when creating new depot
	}

}
