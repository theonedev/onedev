package com.pmease.gitop.model.gatekeeper;

import java.io.Serializable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@Editable
public interface GateKeeper extends Trimmable, Serializable {
	
	CheckResult check(PullRequest request);

}
