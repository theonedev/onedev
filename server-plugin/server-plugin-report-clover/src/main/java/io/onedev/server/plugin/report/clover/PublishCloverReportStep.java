package io.onedev.server.plugin.report.clover;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.coverage.Coverage;
import io.onedev.server.plugin.report.coverage.CoverageInfo;
import io.onedev.server.plugin.report.coverage.CoverageReport;
import io.onedev.server.plugin.report.coverage.FileCoverageInfo;
import io.onedev.server.plugin.report.coverage.PackageCoverageInfo;
import io.onedev.server.plugin.report.coverage.PublishCoverageReportStep;
import io.onedev.server.util.XmlUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=9910, group=StepGroup.PUBLISH_REPORTS, name="Clover Coverage")
public class PublishCloverReportStep extends PublishCoverageReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify clover coverage xml report file under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
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
	protected CoverageReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		int totalStatements = 0;
		int coveredStatements = 0;
		int totalMethods = 0;
		int coveredMethods = 0;
		int totalBranches = 0;
		int coveredBranches = 0;
		int totalLines = 0;
		int coveredLines = 0;
		
		List<PackageCoverageInfo> packageCoverages = new ArrayList<>();
		
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing clover report '" + relativePath + "'...");
			Document doc;
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				for (Element projectElement: doc.getRootElement().elements("project")) {
					Element metricsElement = projectElement.element("metrics");
					
					totalStatements += Integer.parseInt(metricsElement.attributeValue("statements"));
					totalMethods += Integer.parseInt(metricsElement.attributeValue("methods"));
					totalBranches += Integer.parseInt(metricsElement.attributeValue("conditionals"));
					
					coveredStatements += Integer.parseInt(metricsElement.attributeValue("coveredstatements"));
					coveredMethods += Integer.parseInt(metricsElement.attributeValue("coveredmethods"));
					coveredBranches += Integer.parseInt(metricsElement.attributeValue("coveredconditionals"));
					
					for (Element packageElement: projectElement.elements("package")) {
						String packageName = packageElement.attributeValue("name");

						metricsElement = packageElement.element("metrics");
						
						Coverage packageStatementCoverage = new Coverage(
								Integer.parseInt(metricsElement.attributeValue("statements")),
								Integer.parseInt(metricsElement.attributeValue("coveredstatements")));
						Coverage packageMethodCoverage = new Coverage(
								Integer.parseInt(metricsElement.attributeValue("methods")),
								Integer.parseInt(metricsElement.attributeValue("coveredmethods")));
						Coverage packageBranchCoverage = new Coverage(
								Integer.parseInt(metricsElement.attributeValue("conditionals")), 
								Integer.parseInt(metricsElement.attributeValue("coveredconditionals")));

						int packageTotalLines = 0;
						int packageCoveredLines = 0;
					
						List<FileCoverageInfo> fileCoverages = new ArrayList<>();
						for (Element fileElement: packageElement.elements("file")) {
							String fileName = fileElement.attributeValue("name");
							String blobPath = fileElement.attributeValue("path");
							if (build.getJobWorkspace() != null && blobPath.startsWith(build.getJobWorkspace())) { 
								blobPath = blobPath.substring(build.getJobWorkspace().length()+1);

								metricsElement = fileElement.element("metrics");
								
								Coverage fileStatementCoverage = new Coverage(
										Integer.parseInt(metricsElement.attributeValue("statements")),
										Integer.parseInt(metricsElement.attributeValue("coveredstatements")));
								Coverage fileMethodCoverage = new Coverage(
										Integer.parseInt(metricsElement.attributeValue("methods")),
										Integer.parseInt(metricsElement.attributeValue("coveredmethods")));
								Coverage fileBranchCoverage = new Coverage(
										Integer.parseInt(metricsElement.attributeValue("conditionals")), 
										Integer.parseInt(metricsElement.attributeValue("coveredconditionals")));
								
								Map<Integer, CoverageStatus> lineCoverages = new HashMap<>();
								for (Element lineElement: fileElement.elements("line")) {
									int lineNum = Integer.parseInt(lineElement.attributeValue("num")) - 1;
									CoverageStatus prevStatus = lineCoverages.get(lineNum);
									
									String countStr = lineElement.attributeValue("count");
									if (countStr != null)
										lineCoverages.put(lineNum, getCoverageStatus(prevStatus, countStr));
									
									countStr = lineElement.attributeValue("truecount");
									if (countStr != null)
										lineCoverages.put(lineNum, getCoverageStatus(prevStatus, countStr));
									
									countStr = lineElement.attributeValue("falsecount");
									if (countStr != null)
										lineCoverages.put(lineNum, getCoverageStatus(prevStatus, countStr));
								}
								int fileTotalLines = lineCoverages.size();
								int fileCoveredLines = (int) lineCoverages.entrySet().stream().filter(it->it.getValue()!=CoverageStatus.NOT_COVERED).count();
								
								Coverage fileLineCoverage = new Coverage(fileTotalLines, fileCoveredLines);
								
								packageTotalLines += fileTotalLines;
								packageCoveredLines += fileCoveredLines;
								
								if (!lineCoverages.isEmpty())
									writeLineCoverages(build, blobPath, lineCoverages);
								
								fileCoverages.add(new FileCoverageInfo(fileName, 
										fileStatementCoverage, fileMethodCoverage, fileBranchCoverage, fileLineCoverage, 
										blobPath));
							}
						}
						
						Coverage packageLineCoverage = new Coverage(packageTotalLines, packageCoveredLines);
						
						packageCoverages.add(new PackageCoverageInfo(
								packageName, packageStatementCoverage, packageMethodCoverage, 
								packageBranchCoverage, packageLineCoverage, fileCoverages));
						
						totalLines += packageTotalLines;
						coveredLines += packageCoveredLines;
					}
				}
			} catch (DocumentException e) {
				logger.warning("Ignored clover report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (!packageCoverages.isEmpty()) {
			CoverageInfo coverageInfo = new CoverageInfo(
					new Coverage(totalStatements, coveredStatements), 
					new Coverage(totalMethods, coveredMethods), 
					new Coverage(totalBranches, coveredBranches), 
					new Coverage(totalLines, coveredLines));
			
			return new CoverageReport(coverageInfo, packageCoverages);
		} else {
			return null;
		}
	}

	private CoverageStatus getCoverageStatus(CoverageStatus prevStatus, String countStr) {
		int count = Integer.parseInt(countStr);
		if (count != 0) {
			if (prevStatus == null)
				return CoverageStatus.COVERED;
			else if (prevStatus == CoverageStatus.NOT_COVERED)
				return CoverageStatus.PARTIALLY_COVERED;
			else
				return prevStatus; 
		} else { 
			if (prevStatus == null)
				return CoverageStatus.NOT_COVERED;
			else if (prevStatus == CoverageStatus.COVERED)
				return CoverageStatus.PARTIALLY_COVERED;
			else
				return prevStatus;
		}
	}
	
}
