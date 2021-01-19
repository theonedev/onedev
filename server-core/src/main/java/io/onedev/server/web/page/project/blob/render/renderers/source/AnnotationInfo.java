package io.onedev.server.web.page.project.blob.render.renderers.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.onedev.server.code.CodeProblem;

class AnnotationInfo {
	
	Map<Integer, List<CommentInfo>> comments = new HashMap<>();
	
	Map<Integer, List<CodeProblem>> problems = new HashMap<>();
	
	Map<Integer, Integer> coverages = new HashMap<>();
	
}