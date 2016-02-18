package com.pmease.gitplex.core.util.validation;

import java.util.Set;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface RepositoryNameReservation {
	Set<String> getReserved();
}
