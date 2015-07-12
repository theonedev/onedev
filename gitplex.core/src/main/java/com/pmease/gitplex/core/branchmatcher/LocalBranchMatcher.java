package com.pmease.gitplex.core.branchmatcher;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;

@Editable
public interface LocalBranchMatcher extends Serializable {
	boolean matches(String branch);
}
