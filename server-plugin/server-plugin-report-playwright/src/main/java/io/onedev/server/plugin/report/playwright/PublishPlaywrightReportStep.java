package io.onedev.server.plugin.report.playwright;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

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
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.PublishUnitTestReportStep;
import io.onedev.server.codequality.UnitTestReport;
import io.onedev.server.codequality.UnitTestReport.TestCase;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Playwright Test Report")
public class PublishPlaywrightReportStep extends PublishUnitTestReportStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="""
			Specify files to publish relative to <a href='https://docs.onedev.io/concepts#job-workdir'>job working directory</a>.
			Published files should include the Playwright JSON report and all referenced artifacts, such as screenshots and traces.
			For example, run <code>CI=true PLAYWRIGHT_JSON_OUTPUT_NAME=test-results/report.json npx playwright test --reporter=json --output=test-results</code>
			from the job working directory, and use <code>test-results/**</code> as the file pattern to publish the report and artifacts together.
			Use * or ? for pattern matching""")
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
	protected UnitTestReport process(Build build, File inputDir, TaskLogger logger) {
		ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
		File reportDir = new File(build.getDir(), UnitTestReport.CATEGORY + "/" + getReportName());
		FileUtils.deleteDir(reportDir);
		FileUtils.createDir(reportDir);
		try {
			List<TestCase> testCases = new ArrayList<>();
			int baseLen = inputDir.getAbsolutePath().length() + 1;
			for (File file: FileUtils.listFiles(inputDir,
					Lists.newArrayList("*.json", "**/*.json"), Lists.newArrayList())) {
				logger.log("Processing Playwright test report: "
						+ file.getAbsolutePath().substring(baseLen));
				testCases.addAll(PlaywrightReportParser.parse(
						build, mapper.readTree(file), inputDir, reportDir, logger));
			}
			if (!testCases.isEmpty())
				return new UnitTestReport(testCases, true);
			else {
				FileUtils.deleteDir(reportDir);
				return null;
			}
		} catch (Exception e) {
			FileUtils.deleteDir(reportDir);
			throw ExceptionUtils.unchecked(e);
		}
	}

}
