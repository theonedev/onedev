package io.onedev.server.plugin.report.clover;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.coverage.*;
import io.onedev.server.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Clover Coverage Report")
public class PublishCloverReportStep extends PublishCoverageReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify clover coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance, <tt>target/site/clover/clover.xml</tt>. "
			+ "Refer to <a href='https://openclover.org/documentation'>OpenClover documentation</a> "
			+ "on how to generate clover xml file. Use * or ? for pattern match")
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
	protected CoverageReport process(Build build, File inputDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		var overallCoverage = new Coverage();
		
		List<GroupCoverage> packageCoverages = new ArrayList<>();
		Map<String, Map<Integer, CoverageStatus>> coverageStatuses = new HashMap<>();
		
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing clover report '" + relativePath + "'...");
			Document doc;
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				var report = CloverReportParser.parse(build, doc, logger);
				overallCoverage.mergeWith(report.getStats().getOverallCoverage());
				packageCoverages.addAll(report.getStats().getGroupCoverages());
				coverageStatuses.putAll(report.getStatuses());
			} catch (DocumentException e) {
				logger.warning("Ignored clover report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (!packageCoverages.isEmpty()) {
			return new CoverageReport(
					new CoverageStats(overallCoverage, packageCoverages), 
					coverageStatuses);
		} else {
			return null;
		}
	}
	
}
