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
import org.dom4j.Element;
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

import static io.onedev.server.plugin.report.coverage.CoverageInfo.getCoverage;
import static java.lang.Integer.parseInt;

@Editable(order=9910, group=StepGroup.PUBLISH_REPORTS, name="Clover Coverage")
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
					
					totalStatements += parseInt(metricsElement.attributeValue("statements"));
					totalMethods += parseInt(metricsElement.attributeValue("methods"));
					totalBranches += parseInt(metricsElement.attributeValue("conditionals"));
					
					coveredStatements += parseInt(metricsElement.attributeValue("coveredstatements"));
					coveredMethods += parseInt(metricsElement.attributeValue("coveredmethods"));
					coveredBranches += parseInt(metricsElement.attributeValue("coveredconditionals"));
					
					for (Element packageElement: projectElement.elements("package")) {
						String packageName = packageElement.attributeValue("name");

						metricsElement = packageElement.element("metrics");
						
						int packageStatementCoverage = getCoverage(
								parseInt(metricsElement.attributeValue("statements")),
								parseInt(metricsElement.attributeValue("coveredstatements")));
						int packageMethodCoverage = getCoverage(
								parseInt(metricsElement.attributeValue("methods")),
								parseInt(metricsElement.attributeValue("coveredmethods")));
						int packageBranchCoverage = getCoverage(
								parseInt(metricsElement.attributeValue("conditionals")), 
								parseInt(metricsElement.attributeValue("coveredconditionals")));

						int packageTotalLines = 0;
						int packageCoveredLines = 0;
					
						List<FileCoverageInfo> fileCoverages = new ArrayList<>();
						for (Element fileElement: packageElement.elements("file")) {
							String fileName = fileElement.attributeValue("name");
							String blobPath = fileElement.attributeValue("path");
							if (build.getJobWorkspace() != null && blobPath.startsWith(build.getJobWorkspace())) { 
								blobPath = blobPath.substring(build.getJobWorkspace().length()+1);

								metricsElement = fileElement.element("metrics");
								
								int fileStatementCoverage = getCoverage(
										parseInt(metricsElement.attributeValue("statements")),
										parseInt(metricsElement.attributeValue("coveredstatements")));
								int fileMethodCoverage = getCoverage(
										parseInt(metricsElement.attributeValue("methods")),
										parseInt(metricsElement.attributeValue("coveredmethods")));
								int fileBranchCoverage = getCoverage(
										parseInt(metricsElement.attributeValue("conditionals")), 
										parseInt(metricsElement.attributeValue("coveredconditionals")));
								
								Map<Integer, CoverageStatus> lineCoverages = new HashMap<>();
								for (Element lineElement: fileElement.elements("line")) {
									int lineNum = parseInt(lineElement.attributeValue("num")) - 1;
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
								
								int fileLineCoverage = getCoverage(fileTotalLines, fileCoveredLines);
								
								packageTotalLines += fileTotalLines;
								packageCoveredLines += fileCoveredLines;
								
								if (!lineCoverages.isEmpty())
									writeLineCoverages(build, blobPath, lineCoverages);
								
								fileCoverages.add(new FileCoverageInfo(fileName, 
										fileStatementCoverage, fileMethodCoverage, fileBranchCoverage, fileLineCoverage, 
										blobPath));
							}
						}
						
						int packageLineCoverage = getCoverage(packageTotalLines, packageCoveredLines);
						
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
					getCoverage(totalStatements, coveredStatements), 
					getCoverage(totalMethods, coveredMethods), 
					getCoverage(totalBranches, coveredBranches), 
					getCoverage(totalLines, coveredLines));
			
			return new CoverageReport(coverageInfo, packageCoverages);
		} else {
			return null;
		}
	}

	private CoverageStatus getCoverageStatus(CoverageStatus prevStatus, String countStr) {
		int count = parseInt(countStr);
		if (count != 0)
			return CoverageStatus.COVERED.mergeWith(prevStatus);
		else  
			return CoverageStatus.NOT_COVERED.mergeWith(prevStatus);
	}
	
}
