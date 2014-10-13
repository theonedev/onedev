package com.pmease.gitplex.core.branchmatcher;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitplex.core.model.Branch;

@Editable
public interface LocalBranchMatcher extends Trimmable, Serializable {
	boolean matches(Branch branch);
}
