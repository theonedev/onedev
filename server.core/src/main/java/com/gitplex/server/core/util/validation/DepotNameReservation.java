package com.gitplex.server.core.util.validation;

import java.util.Set;

import com.gitplex.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface DepotNameReservation {
	Set<String> getReserved();
}
