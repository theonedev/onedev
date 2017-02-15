package com.gitplex.server.security.privilege;

import java.io.Serializable;

public interface Privilege extends Serializable {

	boolean can(Privilege privilege);
	
}
