package io.onedev.server.plugin.report.roslynator;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Integer.parseInt;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

@Editable(order=8040, group=StepGroup.PUBLISH_REPORTS, name="Roslynator")
public class PublishRoslynatorReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify Roslynator diagnostics output file in XML format relative to <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated with <i>-o</i> option. Use * or ? for pattern match")
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

		Map<String, Optional<String>> blobPaths = new HashMap<>();
		List<CodeProblem> problems = new ArrayList<>();
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing Roslynator report '" + relativePath + "'...");
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				xml = StringUtils.removeBOM(xml);
				Document doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				
				var codeAnalysisElement = doc.getRootElement().element("CodeAnalysis");
				Map<String, String> messages = new HashMap<>();
				for (var diagnosticElement: codeAnalysisElement.element("Summary").elements("Diagnostic")) {
					var id = diagnosticElement.attributeValue("Id");
					var message = escapeHtml5(diagnosticElement.attributeValue("Title"));
					var description = diagnosticElement.elementText("Description");
					if (description != null)
						message += "<br><br>" + escapeHtml5(description);
					messages.put(id, message);
				}
				for (var projectElement: codeAnalysisElement.element("Projects").elements("Project")) {
					for (var diagnosticsElement: projectElement.element("Diagnostics").elements("Diagnostic")) {
						var id = diagnosticsElement.attributeValue("Id");
						var message = messages.get(id);
						if (message == null)
							message = diagnosticsElement.elementText("Message");
						CodeProblem.Severity severity;
						switch (diagnosticsElement.elementText("Severity").trim()) {
							case "Error":
								severity = Severity.HIGH;
								break;
							case "Warning":
								severity = Severity.MEDIUM;
								break;
							default:
								severity = Severity.LOW;
						}
						var filePath = diagnosticsElement.elementText("FilePath").trim();
						var blobPath = blobPaths.get(filePath);
						if (blobPath == null) {
							blobPath = Optional.ofNullable(build.getBlobPath(filePath));
							if (blobPath.isEmpty()) 
								logger.warning("Unable to find blob path for file: " + filePath);
							blobPaths.put(filePath, blobPath);
						}
						if (blobPath.isPresent()) {
							var locationElement = diagnosticsElement.element("Location");
							int line = parseInt(locationElement.attributeValue("Line"));
							int character = parseInt(locationElement.attributeValue("Character"));
							var range = new PlanarRange(line-1, character-1, line-1, character);
							problems.add(new CodeProblem(severity, id, blobPath.get(), range, message));
						}
					}
				}
			} catch (DocumentException e) {
				logger.warning("Ignored Roslynator report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (!problems.isEmpty())
			return new ProblemReport(problems);
		else 
			return null;
	}
	
}
