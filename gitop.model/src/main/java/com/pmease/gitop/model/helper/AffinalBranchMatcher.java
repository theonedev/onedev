package com.pmease.gitop.model.helper;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.Branch;

@Editable
public interface AffinalBranchMatcher extends Trimmable, Serializable {
	boolean matches(Branch branch);
}
