package io.onedev.server.plugin.report.problem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.onedev.server.codequality.CodeProblem;

public class ProblemFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final Collection<CodeProblem> problems = new ArrayList<>();
	
	public ProblemFile(String blobPath) {
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}

	public Collection<CodeProblem> getProblems() {
		return problems;
	}
	
}