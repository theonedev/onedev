package io.onedev.server.plugin.report.junit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.PublishUnitTestReportStep;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.util.XmlUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=7010, name="Publish JUnit Report")
public class PublishJUnitReportStep extends PublishUnitTestReportStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="Specify JUnit test result file in XML format relative to <a href='$docRoot/pages/concepts.md#job-workspace'>job workspace</a>, "
			+ "for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match")
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
		return BuildSpec.suggestVariables(matchWith, true, true);
	}

	@Override
	protected UnitTestReport processReports(Build build, File filesDir, TaskLogger logger) {
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);
		
		List<TestCase> testCases = new ArrayList<>();
		int baseLen = filesDir.getAbsolutePath().length()+1;
		for (File file: FileUtils.listFiles(filesDir, Lists.newArrayList("**"), Lists.newArrayList())) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing JUnit test report '" + relativePath + "'...");
			try {
				testCases.addAll(JUnitReportParser.parse(build, reader.read(file)));
			} catch (DocumentException e) {
				logger.warning("Ignored test report '" + relativePath + "' as it is not a valid XML");
			}
		}
		if (!testCases.isEmpty()) 
			return new UnitTestReport(testCases, true);
		else 
			return null;
	}

}
