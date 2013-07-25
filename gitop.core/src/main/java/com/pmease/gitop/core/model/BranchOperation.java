package com.pmease.gitop.core.model;

public interface BranchOperation {
	boolean implies(BranchOperation branchOperation);
}
