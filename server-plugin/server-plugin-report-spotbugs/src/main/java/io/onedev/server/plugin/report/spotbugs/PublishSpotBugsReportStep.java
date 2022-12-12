package io.onedev.server.plugin.report.spotbugs;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.validation.constraints.NotEmpty;
import org.unbescape.html.HtmlEscape;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.step.StepGroup;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.problem.ProblemReport;
import io.onedev.server.plugin.report.problem.PublishProblemReportStep;
import io.onedev.server.util.XmlUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.Patterns;

@Editable(order=8010, group=StepGroup.PUBLISH_REPORTS, name="SpotBugs")
public class PublishSpotBugsReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify SpotBugs result xml file under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
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
	protected ProblemReport createReport(Build build, File inputDir, File reportDir, TaskLogger logger) {
		int baseLen = inputDir.getAbsolutePath().length() + 1;
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		List<CodeProblem> problems = new ArrayList<>();
		Map<String, List<CodeProblem>> problemsByFile = new HashMap<>();
		for (File file: getPatternSet().listFiles(inputDir)) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing SpotBugs report '" + relativePath + "'...");
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				Document doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				
				Element projectElement = doc.getRootElement().element("Project");
				String srcPath = projectElement.elementText("SrcDir");
				if (build.getJobWorkspace() != null && srcPath.startsWith(build.getJobWorkspace())) {
					srcPath = srcPath.substring(build.getJobWorkspace().length()+1);
					if (srcPath.startsWith("/"))
						srcPath = srcPath.substring(1);
					for (Element bugElement: doc.getRootElement().elements("BugInstance")) {
						Element sourceElement = bugElement.element("SourceLine");
						String blobPath = srcPath + "/" + sourceElement.attributeValue("sourcepath");
						BlobIdent blobIdent = new BlobIdent(build.getCommitHash(), blobPath);
						if (build.getProject().getBlob(blobIdent, false) != null) {
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

							CodeProblem problem = new CodeProblem(severity, type, blobPath, range, message); 
							problems.add(problem);
							List<CodeProblem> problemsOfFile = problemsByFile.get(blobPath);
							if (problemsOfFile == null) {
								problemsOfFile = new ArrayList<>();
								problemsByFile.put(blobPath, problemsOfFile);
							}
							problemsOfFile.add(problem);
						}
					}
				}
			} catch (DocumentException e) {
				logger.warning("Ignored SpotBugs report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (Map.Entry<String, List<CodeProblem>> entry: problemsByFile.entrySet()) 
			writeFileProblems(build, entry.getKey(), entry.getValue());
		
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
