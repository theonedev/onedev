package com.pmease.gitop.core.gatekeeper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.gitop.core.gatekeeper.checkresult.Accepted;
import com.pmease.gitop.core.gatekeeper.checkresult.Blocked;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.gatekeeper.checkresult.Rejected;
import com.pmease.gitop.core.model.MergeRequest;

@SuppressWarnings("serial")
@Editable
@TableLayout
public class IfThenGateKeeper extends AbstractGateKeeper {

	private GateKeeper ifGate;
	
	private GateKeeper thenGate;
	
	@Editable(order=100)
	@NotNull
	@Valid
	public GateKeeper getIfGate() {
		return ifGate;
	}

	public void setIfGate(GateKeeper ifGate) {
		this.ifGate = ifGate;
	}

	@Editable(order=200)
	@NotNull
	@Valid
	public GateKeeper getThenGate() {
		return thenGate;
	}

	public void setThenGate(GateKeeper thenGate) {
		this.thenGate = thenGate;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		CheckResult ifResult = getIfGate().check(request);
		if (ifResult instanceof Accepted) {
			return getThenGate().check(request);
		} else if (ifResult instanceof Rejected) {
			return accepted(ifResult.getReasons());
		} else if (ifResult instanceof Blocked) {
			return ifResult;
		} else {
			CheckResult thenResult = getThenGate().check(request);
			if (thenResult instanceof Accepted)
				return thenResult;
			else 
				return ifResult;
		}
	}

}
