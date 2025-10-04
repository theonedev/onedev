package io.onedev.server.plugin.report.jacoco;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.coverage.Coverage;
import io.onedev.server.plugin.report.coverage.CoverageReport;
import io.onedev.server.plugin.report.coverage.CoverageStats;
import io.onedev.server.plugin.report.coverage.FileCoverage;
import io.onedev.server.plugin.report.coverage.GroupCoverage;
import io.onedev.server.plugin.report.coverage.PublishCoverageReportStep;
import io.onedev.server.util.XmlUtils;

@Editable(order=10000, group=StepGroup.PUBLISH, name="JaCoCo Coverage Report")
public class PublishJacocoReportStep extends PublishCoverageReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify JaCoCo coverage xml report file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance, <tt>target/site/jacoco/jacoco.xml</tt>. Use * or ? for pattern match")
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

		List<GroupCoverage> packageCoverages = new ArrayList<>();
		var coverageInfo = new Coverage();
		
		Map<String, Map<Integer, CoverageStatus>> coverageStatuses = new HashMap<>();
		
		var projectService = OneDev.getInstance(ProjectService.class);
		var repository = projectService.getRepository(build.getProject().getId());
		var blobPaths = GitUtils.getBlobPaths(repository, build.getCommitId());
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing JaCoCo report '" + relativePath + "'...");
			Document doc;
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				for (Element packageElement: doc.getRootElement().elements("package")) {
					String packageName = packageElement.attributeValue("name");
					var packageCoverageInfo = getCoverageInfo(packageElement);
					List<FileCoverage> fileCoverages = new ArrayList<>();
					
					for (Element fileElement: packageElement.elements("sourcefile")) {
						String fileName = fileElement.attributeValue("name");
						var fileCoverageInfo = getCoverageInfo(fileElement);
						String blobPath = null;
						for (var eachBlobPath: blobPaths) {
							if (eachBlobPath.endsWith(packageName + "/" + fileName)) {
								blobPath = eachBlobPath;
								break;
							}
						}
						if (blobPath != null) {
							fileCoverages.add(new FileCoverage(blobPath, fileCoverageInfo));
							Map<Integer, CoverageStatus> coverageStatusesOfFile = new HashMap<>();
							for (Element lineElement: fileElement.elements("line")) {
								int lineNum = Integer.parseInt(lineElement.attributeValue("nr")) - 1;
								CoverageStatus coverageStatus;
								int mi = Integer.parseInt(lineElement.attributeValue("mi"));
								int ci = Integer.parseInt(lineElement.attributeValue("ci"));
								int mb = Integer.parseInt(lineElement.attributeValue("mb"));
								int cb = Integer.parseInt(lineElement.attributeValue("cb"));
								if (mi == 0 && mb == 0)
									coverageStatus = CoverageStatus.COVERED;
								else if (ci == 0 && cb == 0)
									coverageStatus = CoverageStatus.NOT_COVERED;
								else
									coverageStatus = CoverageStatus.PARTIALLY_COVERED;
								if (coverageStatus != CoverageStatus.NOT_COVERED)
									coverageStatusesOfFile.put(lineNum, coverageStatus);
							}
							if (!coverageStatusesOfFile.isEmpty())
								coverageStatuses.put(blobPath, coverageStatusesOfFile);
						} else {
							logger.warning(String.format(
									"Unable to find blob path (package name: %s, file name: %s)", 
									packageName, fileName));
						}
					}
					
					packageCoverages.add(new GroupCoverage(packageName, packageCoverageInfo, fileCoverages));
				}
				coverageInfo.mergeWith(getCoverageInfo(doc.getRootElement()));
			} catch (DocumentException e) {
				logger.warning("Ignored clover report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (!packageCoverages.isEmpty()) {
			return new CoverageReport(
					new CoverageStats(coverageInfo, packageCoverages),
					coverageStatuses);
		} else {
			return null;
		}
	}
	
	private Coverage getCoverageInfo(Element element) {
		int totalBranches = 0;
		int coveredBranches = 0;
		int totalLines = 0;
		int coveredLines = 0;
		for (Element counterElement: element.elements("counter")) {
			int covered = Integer.parseInt(counterElement.attributeValue("covered"));
			int total = covered + Integer.parseInt(counterElement.attributeValue("missed"));
			switch (counterElement.attributeValue("type")) {
				case "BRANCH":
					totalBranches = total;
					coveredBranches = covered;
					break;
				case "LINE":
					totalLines = total;
					coveredLines = covered;
					break;
			}
		}
		return new Coverage(totalBranches, coveredBranches, totalLines, coveredLines);
	}

}
