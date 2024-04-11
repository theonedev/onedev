package io.onedev.server.plugin.report.problem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import io.onedev.server.codequality.CodeProblem;

public class ProblemFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String blobPath;
	
	private final boolean inRepo;
	
	private final Collection<CodeProblem> problems = new ArrayList<>();
	
	public ProblemFile(String blobPath) {
		if (blobPath.startsWith(CodeProblem.NON_REPO_FILE_PREFIX)) {
			this.blobPath = blobPath.substring(CodeProblem.NON_REPO_FILE_PREFIX.length());
			inRepo = false;
		} else {
			this.blobPath = blobPath;
			inRepo = true;
		}
	}

	public String getBlobPath() {
		return blobPath;
	}

	public boolean isInRepo() {
		return inRepo;
	}

	public Collection<CodeProblem> getProblems() {
		return problems;
	}
	
}