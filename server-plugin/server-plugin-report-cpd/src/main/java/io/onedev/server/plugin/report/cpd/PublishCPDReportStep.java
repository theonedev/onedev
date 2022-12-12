package io.onedev.server.plugin.report.cpd;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import javax.validation.constraints.NotEmpty;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Splitter;

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
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

@Editable(order=8030, group=StepGroup.PUBLISH_REPORTS, name="CPD")
public class PublishCPDReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify CPD result xml file under <a href='https://docs.onedev.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance, <tt>target/cpd.xml</tt>. Use * or ? for pattern match")
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
			logger.log("Processing CPD report '" + relativePath + "'...");
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				Document doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));

				for (Element duplicationElement: doc.getRootElement().elements("duplication")) {
					List<CodeDuplication> duplications = new ArrayList<>();
					for (Element fileElement: duplicationElement.elements("file")) {
						String filePath = fileElement.attributeValue("path");
						if (build.getJobWorkspace() != null && filePath.startsWith(build.getJobWorkspace())) {
							String blobPath = filePath.substring(build.getJobWorkspace().length()+1);
							BlobIdent blobIdent = new BlobIdent(build.getCommitHash(), blobPath);
							if (build.getProject().getBlob(blobIdent, false) != null) {
								int beginLine = Integer.parseInt(fileElement.attributeValue("line"));
								int endLine = Integer.parseInt(fileElement.attributeValue("endline"));
								int beginColumn = Integer.parseInt(fileElement.attributeValue("column"));
								int endColumn = Integer.parseInt(fileElement.attributeValue("endcolumn"));
								PlanarRange range = new PlanarRange(beginLine-1, beginColumn-1, endLine-1, endColumn);
								CodeDuplication duplication = new CodeDuplication();
								duplication.blobPath = blobPath;
								duplication.range = range;
								duplications.add(duplication);
							}
						}
					}
					if (duplications.size() >= 2) {
						for (int i=0; i<duplications.size(); i++) {
							CodeDuplication duplication = duplications.get(i);
							CodeDuplication duplicateWith;
							if (i == duplications.size()-1)
								duplicateWith = duplications.get(0);
							else
								duplicateWith = duplications.get(i+1);
							
							PageParameters params = new PageParameters();
							ProjectBlobPage.State state = new ProjectBlobPage.State();
							state.blobIdent = new BlobIdent();
							state.problemReport = getReportName();
							state.position = BlobRenderer.getSourcePosition(duplicateWith.range); 
							
							params.set(0, build.getCommitHash());
							List<String> pathSegments = Splitter.on("/").splitToList(duplicateWith.blobPath);
							for (int j=0; j<pathSegments.size(); j++) 
								params.set(j+1, pathSegments.get(j));
							
							ProjectBlobPage.fillParams(params, state);
							
							PageParametersEncoder paramsEncoder = new PageParametersEncoder();
							String url  = "/" + build.getProject().getPath() + "/~files/" + paramsEncoder.encodePageParameters(params);
							String message = String.format(""
									+ "Duplicated with '%s' at <a href='%s'>line %s - %s</a>", 
									HtmlEscape.escapeHtml5(duplicateWith.blobPath), url, duplicateWith.range.getFromRow()+1, 
									duplicateWith.range.getToRow()+1);
							CodeProblem problem = new CodeProblem(Severity.LOW, "Code Duplication", duplication.blobPath, 
									duplication.range, message);
							problems.add(problem);
							List<CodeProblem> problemsOfFile = problemsByFile.get(problem.getBlobPath());
							if (problemsOfFile == null) {
								problemsOfFile = new ArrayList<>();
								problemsByFile.put(problem.getBlobPath(), problemsOfFile);
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

	private static class CodeDuplication {
		
		String blobPath;
		
		PlanarRange range;
		
	}
}
