package com.pmease.gitop.core.gatekeeper;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

@Editable
public interface GateKeeper extends Trimmable, Serializable {
	
	CheckResult check(MergeRequest request);
}
