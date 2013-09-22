package com.pmease.gitop.core.gatekeeper;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public abstract class AbstractGateKeeper implements GateKeeper {

	@Override
	public Object trim(@Nullable Object context) {
		return this;
	}

	protected CheckResult accept(String reason) {
		return new CheckResult.Accept(reason);
	}

	protected CheckResult reject(String reason) {
		return new CheckResult.Reject(reason);
	}

	protected CheckResult pending(String reason) {
		return new CheckResult.Pending(reason);
	}

	protected CheckResult block(String reason) {
		return new CheckResult.Block(reason);
	}

	protected CheckResult accept(List<String> reasons) {
		return new CheckResult.Accept(reasons);
	}

	protected CheckResult reject(List<String> reasons) {
		return new CheckResult.Reject(reasons);
	}

	protected CheckResult pending(List<String> reasons) {
		return new CheckResult.Pending(reasons);
	}

	protected CheckResult block(List<String> reasons) {
		return new CheckResult.Block(reasons);
	}

}
