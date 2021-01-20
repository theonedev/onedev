package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.onedev.server.code.CodeProblem;

public class AnnotationInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final Map<Integer, List<CodeCommentInfo>> comments;
	
	private final Map<Integer, List<CodeProblem>> problems;
	
	private final Map<Integer, Integer> coverages;
	
	public AnnotationInfo(
			Map<Integer, List<CodeCommentInfo>> comments, 
			Map<Integer, List<CodeProblem>> problems,
			Map<Integer, Integer> coverages) {
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

	public Map<Integer, Integer> getCoverages() {
		return coverages;
	}
	
}