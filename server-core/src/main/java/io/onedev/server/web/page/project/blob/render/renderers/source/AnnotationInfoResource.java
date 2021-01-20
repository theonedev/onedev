package io.onedev.server.web.page.project.blob.render.renderers.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jgit.lib.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.code.LineCoverage;
import io.onedev.server.code.LineCoverageContribution;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

class AnnotationInfoResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_COMMIT = "commit";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_PROBLEM_REPORT = "problem-report";
	
	private static final String PARAM_COVERAGE_REPORT = "coverage-report";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		ObjectId commitId = ObjectId.fromString(params.get(PARAM_COMMIT).toString());
		String path = params.get(PARAM_PATH).toString();
		String problemReport = params.get(PARAM_PROBLEM_REPORT).toOptionalString();
		String coverageReport = params.get(PARAM_COVERAGE_REPORT).toOptionalString();

		ResourceResponse response = new ResourceResponse();
		response.setContentType("application/json");
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				Project project = OneDev.getInstance(Dao.class).load(Project.class, projectId);
				
				if (!SecurityUtils.canReadCode(project))
					throw new UnauthorizedException();
	
				AnnotationInfo annotationInfo = new AnnotationInfo();
				
				CodeCommentManager codeCommentManager = OneDev.getInstance(CodeCommentManager.class);
				for (Map.Entry<CodeComment, PlanarRange> entry: codeCommentManager
						.queryInHistory(project, commitId, path).entrySet()) {
					CodeComment comment = entry.getKey();
					PlanarRange position = entry.getValue();
					int line = position.getFromRow();
					List<CommentInfo> commentsAtLine = annotationInfo.comments.get(line);
					if (commentsAtLine == null) {
						commentsAtLine = new ArrayList<>();
						annotationInfo.comments.put(line, commentsAtLine);
					}
					CommentInfo commentInfo = new CommentInfo(comment, position);
					commentsAtLine.add(commentInfo);
				}
				for (List<CommentInfo> value: annotationInfo.comments.values()) {
					value.sort((o1, o2)->(int)(o1.id-o2.id));
				}

				Set<CodeProblem> problemDuplicationCheck = new HashSet<>();
				
				BuildManager buildManager = OneDev.getInstance(BuildManager.class);
				for (Build build: buildManager.query(project, commitId, null, null, null, new HashMap<>())) {
					for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class)) {
						for (CodeProblem problem: contribution.getCodeProblems(build, path, problemReport)) {
							if (problemDuplicationCheck.add(problem)) {
								PlanarRange position = problem.getPosition();
								int line = position.getFromRow();
								List<CodeProblem> problemsAtLine = annotationInfo.problems.get(line);
								if (problemsAtLine == null) {
									problemsAtLine = new ArrayList<>();
									annotationInfo.problems.put(line, problemsAtLine);
								}
								problemsAtLine.add(problem);
							}
						}
					}
					
					for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
						for (LineCoverage coverage: contribution.getLineCoverages(build, path, coverageReport)) {
							for (int line = coverage.getFromLine(); line <= coverage.getToLine(); line++) {
								Integer testCount = annotationInfo.coverages.get(line);
								if (testCount != null) 
									annotationInfo.coverages.put(line, testCount + coverage.getTestCount());
								else
									annotationInfo.coverages.put(line, coverage.getTestCount());
							}
						}
					}
				}
				for (List<CodeProblem> value: annotationInfo.problems.values()) {
					value.sort((o1, o2)->(int)(o1.getSeverity().ordinal()-o2.getSeverity().ordinal()));
				}
				
				ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
				attributes.getResponse().write(mapper.writeValueAsString(annotationInfo));
			}
			
		});

		return response;
	}

	public static PageParameters paramsOf(Project project, String commitHash, String path, 
			@Nullable String problemReport, @Nullable String coverageReport) {
		PageParameters params = new PageParameters();
		params.add(PARAM_PROJECT, project.getId());
		params.add(PARAM_COMMIT, commitHash);
		params.add(PARAM_PATH, path);
		
		if (problemReport != null)
			params.add(PARAM_PROBLEM_REPORT, problemReport);
		if (coverageReport != null)
			params.add(PARAM_COVERAGE_REPORT, coverageReport);
		
		return params;
	}
	
}
