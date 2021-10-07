package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CoverageStatus;

public class AnnotationInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Map<Integer, List<CodeCommentInfo>> comments;
	
	private final Map<Integer, List<CodeProblem>> problems;
	
	private final Map<Integer, CoverageStatus> coverages;
	
	public AnnotationInfo(
			Map<Integer, List<CodeCommentInfo>> comments, 
			Map<Integer, List<CodeProblem>> problems,
			Map<Integer, CoverageStatus> coverages) {
		this.comments = comments;
		this.problems = problems;
		this.coverages = coverages;
	}

	public Map<Integer, List<CodeCommentInfo>> getComments() {
		return comments;
	}

	public Map<Integer, List<CodeProblem>> getProblems() {
		return problems;
	}

	public Map<Integer, CoverageStatus> getCoverages() {
		return coverages;
	}
	
}