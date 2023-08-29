package io.onedev.server.plugin.report.cobertura;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.coverage.*;
import io.onedev.server.search.code.CodeSearchManager;
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

import static io.onedev.server.codequality.CoverageStatus.*;
import static io.onedev.server.plugin.report.coverage.CoverageInfo.getCoverage;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

@Editable(order=9950, group=StepGroup.PUBLISH_REPORTS, name="Cobertura Coverage")
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
	public boolean requireCommitIndex() {
		return true;
	}

	@Override
	protected CoverageReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		int totalBranches = 0;
		int coveredBranches = 0;
		int totalLines = 0;
		int coveredLines = 0;
		
		List<CategoryCoverageInfo> packageCoverages = new ArrayList<>();
		
		var searchManager = OneDev.getInstance(CodeSearchManager.class);
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
				
				for (var packageElement: coverageElement.element("packages").elements()) {
					String packageName = packageElement.attributeValue("name");
					if (packageName.length() == 0)
						packageName = "[default]";
					List<ItemCoverageInfo> classCoverages = new ArrayList<>();

					Map<String, Map<Integer, CoverageStatus>> lineCoverages = new HashMap<>();
					for (var classElement: packageElement.element("classes").elements()) {
						var className = classElement.attributeValue("name");
						var blobPath = searchManager.findBlobPathBySymbol(build.getProject(), build.getCommitId(), className, ".");
						if (blobPath != null) {
							Map<Integer, CoverageStatus> lineCoveragesOfFile = lineCoverages.computeIfAbsent(blobPath, it -> new HashMap<>());
							for (var lineElement: classElement.element("lines").elements()) {
								var lineNum = parseInt(lineElement.attributeValue("number")) - 1;
								var lineHits = parseInt(lineElement.attributeValue("hits"));
								CoverageStatus status;
								if (lineHits == 0) {
									status = NOT_COVERED;
								} else {
									var branch = parseBoolean(lineElement.attributeValue("branch"));
									if (branch) {
										var conditionCoverage = lineElement.attributeValue("condition-coverage");
										if (conditionCoverage.startsWith("100%"))
											status = COVERED;
										else
											status = PARTIALLY_COVERED;
									} else {
										status = COVERED;
									}
								}
								if (status != NOT_COVERED)
									lineCoveragesOfFile.put(lineNum, status);
							}

							int fileLineCoverage = (int)(parseDouble(classElement.attributeValue("line-rate")) * 100);
							int fileBranchCoverage = (int)(parseDouble(classElement.attributeValue("branch-rate")) * 100);
							classCoverages.add(new ItemCoverageInfo(
									className,
									-1, -1, 
									fileBranchCoverage, fileLineCoverage,
									blobPath));
						}
					}

					for (var entry: lineCoverages.entrySet()) 
						writeLineCoverages(build, entry.getKey(), entry.getValue());

					int packageLineCoverage = (int)(parseDouble(packageElement.attributeValue("line-rate")) * 100);
					int packageBranchCoverage = (int)(parseDouble(packageElement.attributeValue("branch-rate")) * 100);
					packageCoverages.add(new CategoryCoverageInfo(
							packageName, -1, -1, 
							packageBranchCoverage, packageLineCoverage, classCoverages));
				}
			} catch (DocumentException e) {
				logger.warning("Ignored cobertura report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (!packageCoverages.isEmpty()) {
			CoverageInfo coverageInfo = new CoverageInfo(
					-1, 
					-1,
					getCoverage(totalBranches, coveredBranches), 
					getCoverage(totalLines, coveredLines));
			
			return new CoverageReport(coverageInfo, packageCoverages);
		} else {
			return null;
		}
	}
	
}
