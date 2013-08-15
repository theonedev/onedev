package com.pmease.gitop.core.model.permission;

public interface PrivilegedOperation {
	boolean can(PrivilegedOperation operation);
}
