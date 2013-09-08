package com.pmease.gitop.core.permission.operation;

import java.io.Serializable;

public interface PrivilegedOperation extends Serializable {
	boolean can(PrivilegedOperation operation);
}
