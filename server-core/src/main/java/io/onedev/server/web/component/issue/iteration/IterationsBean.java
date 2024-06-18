package io.onedev.server.web.component.issue.iteration;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IterationChoice;

import java.io.Serializable;
import java.util.List;

@Editable
public class IterationsBean implements Serializable {
	
	private List<String> iterationNames;

	@Editable
	@IterationChoice
	public List<String> getIterationNames() {
		return iterationNames;
	}

	public void setIterationNames(List<String> iterationNames) {
		this.iterationNames = iterationNames;
	}

}
