package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.gitop.core.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Pending;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.gatekeeper.voteeligibility.VoteEligibility;

@SuppressWarnings("serial")
@Editable
public abstract class AbstractGateKeeper implements GateKeeper {

	@Override
	public Object trim(@Nullable Object context) {
		return this;
	}

	protected CheckResult accepted(String reason) {
		return new Accepted(reason);
	}

	protected CheckResult rejected(String reason) {
		return new Rejected(reason);
	}

	protected CheckResult pending(String reason, VoteEligibility voteEligibility) {
		return new Pending(reason, voteEligibility);
	}

	protected CheckResult blocked(String reason, VoteEligibility voteEligibility) {
		return new Blocked(reason, voteEligibility);
	}

	protected CheckResult accepted(List<String> reasons) {
		return new Accepted(reasons);
	}

	protected CheckResult rejected(List<String> reasons) {
		return new Rejected(reasons);
	}

	protected CheckResult pending(List<String> reasons, Collection<VoteEligibility> voteEligibilies) {
		return new Pending(reasons, voteEligibilies);
	}

	protected CheckResult blocked(List<String> reasons, Collection<VoteEligibility> voteEligibilities) {
		return new Blocked(reasons, voteEligibilities);
	}

}
