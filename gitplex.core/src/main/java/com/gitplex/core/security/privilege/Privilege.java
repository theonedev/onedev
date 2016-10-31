package com.gitplex.core.security.privilege;

import java.io.Serializable;

public interface Privilege extends Serializable {

	boolean can(Privilege privilege);
	
}
