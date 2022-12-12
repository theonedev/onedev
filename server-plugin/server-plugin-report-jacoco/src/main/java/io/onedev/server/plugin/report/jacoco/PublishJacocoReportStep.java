package io.onedev.server.plugin.report.jacoco;

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
import io.onedev.server.OneDev;
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
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.util.XmlUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=9920, group=StepGroup.PUBLISH_REPORTS, name="JaCoCo Coverage")
public class PublishJacocoReportStep extends PublishCoverageReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify JaCoCo coverage xml report file under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
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
	protected CoverageReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		List<PackageCoverageInfo> packageCoverages = new ArrayList<>();
		CoverageInfo coverageInfo = new CoverageInfo(new Coverage(0, 0), new Coverage(0, 0), 
				new Coverage(0, 0), new Coverage(0, 0));
		
		CodeSearchManager searchManager = OneDev.getInstance(CodeSearchManager.class);
		
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing JaCoCo report '" + relativePath + "'...");
			Document doc;
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				for (Element packageElement: doc.getRootElement().elements("package")) {
					String packageName = packageElement.attributeValue("name");
					CoverageInfo packageCoverageInfo = getCoverageInfo(packageElement);
					List<FileCoverageInfo> fileCoverages = new ArrayList<>();
					
					for (Element fileElement: packageElement.elements("sourcefile")) {
						String fileName = fileElement.attributeValue("name");
						CoverageInfo fileCoverageInfo = getCoverageInfo(fileElement);
						String blobPath = searchManager.findBlobPath(build.getProject(), build.getCommitId(), 
								fileName, packageName + "/" + fileName);
						if (blobPath != null) {
							fileCoverages.add(new FileCoverageInfo(fileName, fileCoverageInfo, blobPath));
							Map<Integer, CoverageStatus> lineCoverages = new HashMap<>();
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
								lineCoverages.put(lineNum, coverageStatus);
							}
							writeLineCoverages(build, blobPath, lineCoverages);
						} else {
							logger.warning("Can not map file '" + fileName + "' under package '" 
									+ packageName + "' to blob path, ignoring coverage info...");
						}
					}
					
					packageCoverages.add(new PackageCoverageInfo(packageName, packageCoverageInfo, fileCoverages));
				}
				coverageInfo = coverageInfo.mergeWith(getCoverageInfo(doc.getRootElement()));
			} catch (DocumentException e) {
				logger.warning("Ignored clover report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		if (!packageCoverages.isEmpty()) 
			return new CoverageReport(coverageInfo, packageCoverages);
		else 
			return null;
	}
	
	private CoverageInfo getCoverageInfo(Element element) {
		Coverage statementCoverage = new Coverage(0, 0);
		Coverage branchCoverage = new Coverage(0, 0);
		Coverage lineCoverage = new Coverage(0, 0);
		Coverage methodCoverage = new Coverage(0, 0);
		for (Element counterElement: element.elements("counter")) {
			int covered = Integer.parseInt(counterElement.attributeValue("covered"));
			int total = covered + Integer.parseInt(counterElement.attributeValue("missed"));
			switch (counterElement.attributeValue("type")) {
			case "INSTRUCTION":
				statementCoverage = new Coverage(total, covered);
				break;
			case "BRANCH":
				branchCoverage = new Coverage(total, covered);
				break;
			case "LINE":
				lineCoverage = new Coverage(total, covered);
				break;
			case "METHOD":
				methodCoverage = new Coverage(total, covered);
				break;
			}
		}
		return new CoverageInfo(statementCoverage, methodCoverage, branchCoverage, lineCoverage);
	}

}
