package com.pmease.gitop.model.validation;

import java.util.Set;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface RepositoryNameReservation {
	Set<String> getReserved();
}
