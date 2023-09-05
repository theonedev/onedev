package io.onedev.server.plugin.report.spotbugs;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.problem.ProblemReport;
import io.onedev.server.plugin.report.problem.PublishProblemReportStep;
import io.onedev.server.util.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Editable(order=8010, group=StepGroup.PUBLISH_REPORTS, name="SpotBugs")
public class PublishSpotBugsReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify SpotBugs result xml file relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance, <tt>target/spotbugsXml.xml</tt>. Use * or ? for pattern match")
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
	protected ProblemReport process(Build build, File inputDir, File reportDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		List<CodeProblem> problems = new ArrayList<>();
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing SpotBugs report '" + relativePath + "'...");
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				Document doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				
				Element projectElement = doc.getRootElement().element("Project");
				var srcDir = projectElement.elementText("SrcDir");
				for (Element bugElement: doc.getRootElement().elements("BugInstance")) {
					Element sourceElement = bugElement.element("SourceLine");
					String filePath = srcDir + "/" + sourceElement.attributeValue("sourcepath");
					var blobPath = build.getBlobPath(filePath); 
					if (blobPath != null) {
						String type = bugElement.attributeValue("type");
						
						Severity severity;
						String priority = bugElement.attributeValue("priority");
						if (priority.equals("1"))
							severity = Severity.HIGH;
						else if (priority.equals("2"))
							severity = Severity.MEDIUM;
						else
							severity = Severity.LOW;
						
						String message = bugElement.elementText("LongMessage");
						if (StringUtils.isBlank(message))
							message = bugElement.elementText("ShortMessage");
						
						message = HtmlEscape.escapeHtml5(message);
						
						PlanarRange range = getRange(bugElement, true);

						if (range == null)
							range = getRange(bugElement.element("Field"), false);
						if (range == null)
							range = getRange(bugElement.element("Method"), false);
						if (range == null)
							range = getRange(bugElement.element("Class"), false);
						if (range == null) 
							range = new PlanarRange(0, -1, 0, -1);

						problems.add(new CodeProblem(severity, type, blobPath, range, message));
					} else {
						logger.warning("Unable to find blob path for file: " + filePath);
					}
				}
			} catch (DocumentException e) {
				logger.warning("Ignored SpotBugs report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (!problems.isEmpty())
			return new ProblemReport(problems);
		else
			return null;
	}

	@Nullable
	private PlanarRange getRange(@Nullable Element element, boolean isOriginal) {
		if (element != null) {
			Element sourceElement = element.element("SourceLine");
			String start = sourceElement.attributeValue("start");
			String end = sourceElement.attributeValue("end");
			if (start != null && end != null) {
				int startLine = Integer.parseInt(start)-1;
				int endLine = Integer.parseInt(end)-1;
				if (isOriginal)
					return new PlanarRange(startLine, -1, endLine, -1);
				else
					return new PlanarRange(startLine, -1, startLine, -1);
			}
		}
		return null;
	}
	
}
