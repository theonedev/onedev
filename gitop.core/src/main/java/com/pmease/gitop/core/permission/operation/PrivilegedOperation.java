package com.pmease.gitop.core.permission.operation;

public interface PrivilegedOperation {
	boolean can(PrivilegedOperation operation);
}
