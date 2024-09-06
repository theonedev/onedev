package io.onedev.server.plugin.report.pylint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.problem.PublishProblemReportStep;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Pylint Report")
public class PublishPylintReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify pylint json result file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated with pylint json output format option, for instance <code>--exit-zero --output-format=json:pylint-result.json</code>. "
			+ "Note that we do not fail pylint command upon violations, as this step will fail build based on configured threshold. Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Override
	protected List<CodeProblem> process(Build build, File inputDir, File reportDir, TaskLogger logger) {
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);

		List<CodeProblem> problems = new ArrayList<>();
		int baseLen = inputDir.getAbsolutePath().length()+1;
		for (File file: FileUtils.listFiles(inputDir, Lists.newArrayList("**"), Lists.newArrayList())) {
			logger.log("Processing pylint report: " + file.getAbsolutePath().substring(baseLen));
			try {
				problems.addAll(PylintReportParser.parse(build, mapper.readTree(file), logger));
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		return problems;
	}

}
