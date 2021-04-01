package io.onedev.server.buildspec.step;

import java.io.Serializable;

import io.onedev.k8shelper.Action;
import io.onedev.k8shelper.Executable;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean alwaysExecute;
	
	public abstract Executable getExecutable(BuildSpec buildSpec);

	@Editable(order=100000)
	public boolean isAlwaysExecute() {
		return alwaysExecute;
	}

	public void setAlwaysExecute(boolean alwaysExecute) {
		this.alwaysExecute = alwaysExecute;
	}
	
	public Action getAction(BuildSpec buildSpec) {
		return new Action(alwaysExecute, getExecutable(buildSpec));
	}
}
