package com.gitplex.server.core.util.validation;

import java.util.Set;

import com.gitplex.calla.loader.ExtensionPoint;

@ExtensionPoint
public interface TeamNameReservation {
	Set<String> getReserved();
}
