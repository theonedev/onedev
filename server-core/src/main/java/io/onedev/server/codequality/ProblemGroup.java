package io.onedev.server.codequality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class ProblemGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private final ProblemTarget.GroupKey key;
	
	private final Collection<CodeProblem> problems = new ArrayList<>();
	
	public ProblemGroup(ProblemTarget.GroupKey key) {
		this.key = key;
	}

	public ProblemTarget.GroupKey getKey() {
		return key;
	}

	public Collection<CodeProblem> getProblems() {
		return problems;
	}
	
}
