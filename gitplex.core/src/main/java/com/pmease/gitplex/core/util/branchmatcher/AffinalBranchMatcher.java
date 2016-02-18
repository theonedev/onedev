package com.pmease.gitplex.core.util.branchmatcher;

import java.io.Serializable;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.model.Depot;

@Editable
public interface AffinalBranchMatcher extends Serializable, Trimmable {
	boolean matches(Depot depot, String branch);
}
