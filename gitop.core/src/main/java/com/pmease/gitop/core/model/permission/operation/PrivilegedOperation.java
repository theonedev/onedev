package com.pmease.gitop.core.model.permission.operation;

public interface PrivilegedOperation {
	boolean can(PrivilegedOperation operation);
}
