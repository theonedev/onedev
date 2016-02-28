package com.pmease.gitplex.core.permission.privilege;

import java.io.Serializable;

public interface Privilege extends Serializable {
	boolean can(Privilege privilege);
}
