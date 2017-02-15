package com.gitplex.server.util.validation;

import java.util.Set;

import com.gitplex.launcher.loader.ExtensionPoint;

@ExtensionPoint
public interface TeamNameReservation {
	Set<String> getReserved();
}
