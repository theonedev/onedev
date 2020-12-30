package io.onedev.server.plugin.report.jest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobReport;
import io.onedev.server.model.Build;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.util.validation.annotation.PathSegment;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(name="Jest Test Report")
public class JobJestReport extends JobReport {

	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "jest-reports";
	
	private String reportName;
	
	@Editable(order=100, description="Specify json file containing Jest test results relative to OneDev workspace. "
			+ "It can be generated via Jest option <tt>--json</tt> and <tt>--outputFile</tt>. Use * or ? for pattern match. "
			+ "<b>Note:</b> Type <tt>@</tt> to <a href='$docRoot/pages/variable-substitution.md' target='_blank' tabindex='-1'>insert variable</a>, use <tt>\\</tt> to escape normal occurrences of <tt>@</tt> or <tt>\\</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(interpolative = true, path=true)
	@NotEmpty
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}
	
	@Editable(order=1000, description="Specify report name. "
			+ "<b>Note:</b> Type <tt>@</tt> to <a href='$docRoot/pages/variable-substitution.md' target='_blank' tabindex='-1'>insert variable</a>, use <tt>\\</tt> to escape normal occurrences of <tt>@</tt> or <tt>\\</tt>")
	@Interpolative(variableSuggester="suggestVariables")
	@PathSegment
	@NotEmpty
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return Job.suggestVariables(matchWith);
	}

	@Override
	public void process(Build build, File workspace, SimpleLogger logger) {
		File reportDir = new File(build.getReportDir(DIR), getReportName());
		FileUtils.createDir(reportDir);

		LockUtils.write(build.getReportLockKey(DIR), new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				ObjectMapper mapper = OneDev.getInstance(ObjectMapper.class);
				
				Collection<JsonNode> rootNodes = new ArrayList<>();
				int baseLen = workspace.getAbsolutePath().length() + 1;
				for (File file: getPatternSet().listFiles(workspace)) {
					try {
						rootNodes.add(mapper.readTree(file));
					} catch (Exception e) {
						logger.log("Failed to process Jest report: " + file.getAbsolutePath().substring(baseLen), e);
					}
				}
				if (!rootNodes.isEmpty())
					new JestReportData(build, rootNodes).writeTo(reportDir);
				return null;
			}
			
		});
	}

}
