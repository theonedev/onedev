package com.gitplex.core.util.validation;

import java.util.Set;

import com.gitplex.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface AccountNameReservation {
	Set<String> getReserved();
}
