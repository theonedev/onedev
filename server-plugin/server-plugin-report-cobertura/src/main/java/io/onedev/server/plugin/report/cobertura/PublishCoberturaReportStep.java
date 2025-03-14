package io.onedev.server.plugin.report.cobertura;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
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
import java.util.*;

import static io.onedev.server.codequality.CoverageStatus.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparing;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Cobertura Coverage Report")
public class PublishCoberturaReportStep extends PublishCoverageReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify cobertura coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance, <tt>target/site/cobertura/coverage.xml</tt>. Use * or ? for pattern match")
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

		int totalBranches = 0;
		int coveredBranches = 0;
		int totalLines = 0;
		int coveredLines = 0;
		
		Map<String, GroupCoverage> packageCoverageMap = new HashMap<>();
		Map<String, Map<Integer, CoverageStatus>> coverageStatuses = new HashMap<>();
		
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing cobertura report '" + relativePath + "'...");
			Document doc;
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				xml = StringUtils.removeBOM(xml);
				doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				var coverageElement = doc.getRootElement();
				
				totalBranches += parseInt(coverageElement.attributeValue("branches-valid"));
				coveredBranches += parseInt(coverageElement.attributeValue("branches-covered"));
				totalLines += parseInt(coverageElement.attributeValue("lines-valid"));
				coveredLines += parseInt(coverageElement.attributeValue("lines-covered"));
				
				var sourcePaths = new ArrayList<String>();
				var sourcesElement = coverageElement.element("sources");
				if (sourcesElement != null) {
					for (var sourceElement: sourcesElement.elements()) 
						sourcePaths.add(sourceElement.getText().trim());
				}
				
				Map<String, Optional<String>> blobPaths = new HashMap<>();
				
				for (var packageElement: coverageElement.element("packages").elements()) {
					String packageName = packageElement.attributeValue("name");
					if (packageName.length() == 0)
						packageName = "[default]";
					
					Set<String> packageBlobPaths = new HashSet<>();
					Map<String, Integer> fileTotalBranches = new HashMap<>();
					Map<String, Integer> fileCoveredBranches = new HashMap<>();
					Map<String, Integer> fileTotalLines = new HashMap<>();
					Map<String, Integer> fileCoveredLines = new HashMap<>();
					
					for (var classElement: packageElement.element("classes").elements()) {
						var fileName = classElement.attributeValue("filename");
						var blobPathOpt = blobPaths.get(fileName);
						if (blobPathOpt == null) {
							String blobPath;
							if (sourcePaths.isEmpty()) {
								blobPath = build.getBlobPath(fileName);
							} else {
								blobPath = null;
								for (var sourcePath: sourcePaths) {
									blobPath = build.getBlobPath(sourcePath + "/" + fileName);
									if (blobPath != null) 
										break;
								}
							}
							if (blobPath == null) 
								logger.warning("Unable to find blob path for file: " + fileName);
							blobPathOpt = Optional.ofNullable(blobPath);
							blobPaths.put(fileName, blobPathOpt);
						}
						var blobPath = blobPathOpt.orElse(null);
						if (blobPath != null) {
							packageBlobPaths.add(blobPath);
							Map<Integer, CoverageStatus> coverageStatusesOfFile = coverageStatuses.get(blobPath);
							if (coverageStatusesOfFile == null)
								coverageStatusesOfFile = new HashMap<>();
							int classTotalLines = 0;
							int classCoveredLines = 0;
							int classTotalBranches = 0;
							int classCoveredBranches = 0;
							for (var lineElement: classElement.element("lines").elements()) {
								classTotalLines++;
								var branch = parseBoolean(lineElement.attributeValue("branch"));
								var conditionCoverage = lineElement.attributeValue("condition-coverage");
								if (branch) {
									var branchInfo = StringUtils.substringAfter(conditionCoverage, "(");
									branchInfo = StringUtils.substringBefore(branchInfo, ")");
									classCoveredBranches += parseInt(StringUtils.substringBefore(branchInfo, "/"));
									classTotalBranches += parseInt(StringUtils.substringAfter(branchInfo, "/"));
								}
								
								CoverageStatus status;
								var lineNum = parseInt(lineElement.attributeValue("number")) - 1;
								var lineHits = parseInt(lineElement.attributeValue("hits"));
								if (lineHits == 0) {
									status = NOT_COVERED;
								} else {
									classCoveredLines++;
									if (branch) {
										if (conditionCoverage.startsWith("100%"))
											status = COVERED;
										else
											status = PARTIALLY_COVERED;
									} else {
										status = COVERED;
									}
								}
								coverageStatusesOfFile.put(lineNum, status.mergeWith(coverageStatusesOfFile.get(lineNum)));
							}
							
							if (!coverageStatusesOfFile.isEmpty())
								coverageStatuses.put(blobPath, coverageStatusesOfFile);

							increase(fileTotalBranches, blobPath, classTotalBranches);
							increase(fileCoveredBranches, blobPath, classCoveredBranches);
							increase(fileTotalLines, blobPath, classTotalLines);
							increase(fileCoveredLines, blobPath, classCoveredLines);
						}
					}

					int packageTotalBranches = fileTotalBranches.values().stream().mapToInt(Integer::intValue).sum();
					int packageCoveredBranches = fileCoveredBranches.values().stream().mapToInt(Integer::intValue).sum();
					int packageTotalLines = fileTotalLines.values().stream().mapToInt(Integer::intValue).sum();
					int packageCoveredLines = fileCoveredLines.values().stream().mapToInt(Integer::intValue).sum();

					var fileCoverages = new ArrayList<FileCoverage>();
					for (var blobPath: packageBlobPaths) {
						var fileCoverage = new FileCoverage(blobPath,
								fileTotalBranches.computeIfAbsent(blobPath, it -> 0),
								fileCoveredBranches.computeIfAbsent(blobPath, it -> 0),
								fileTotalLines.computeIfAbsent(blobPath, it -> 0),
								fileCoveredLines.computeIfAbsent(blobPath, it -> 0));
						if (fileCoverage.getLinePercentage() != 0)
							fileCoverages.add(fileCoverage);
					}
					if (packageCoveredLines != 0) {
						var packageCoverage = packageCoverageMap.computeIfAbsent(packageName, GroupCoverage::new);
						packageCoverage.mergeWith(new GroupCoverage(
								packageName, packageTotalBranches, packageCoveredBranches,
								packageTotalLines, packageCoveredLines, fileCoverages));
					}
				}
			} catch (DocumentException e) {
				logger.warning("Ignored cobertura report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (!packageCoverageMap.isEmpty()) {
			Coverage coverageInfo = new Coverage(
					totalBranches, coveredBranches, 
					totalLines, coveredLines);
			var packageCoverages = new ArrayList<>(packageCoverageMap.values());
			packageCoverages.sort(comparing(GroupCoverage::getName));
			for (var packageCoverage: packageCoverages) 
				packageCoverage.getFileCoverages().sort(comparing(FileCoverage::getBlobPath));
			return new CoverageReport(
					new CoverageStats(coverageInfo, packageCoverages), coverageStatuses);
		} else {
			return null;
		}
	}
	
	private void increase(Map<String, Integer> map, String key, int increment) {
		var value = map.get(key);	
		if (value == null)
			value = 0;
		map.put(key, value + increment);
	}
	
}
