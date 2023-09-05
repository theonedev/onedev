package io.onedev.server.plugin.report.trx;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.StringTransformer;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.onedev.server.plugin.report.unittest.UnitTestReport.Status.getOverallStatus;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class TRXReportParser {

	private static final Pattern PATTERN_LOCATION = Pattern.compile("\\sin\\s(.*):line\\s(\\d+)(\\s|$)", Pattern.MULTILINE);

	public static List<TestCase> parse(Build build, Document doc) {
		List<TestCase> testCases = new ArrayList<>();
		Element testRunElement = doc.getRootElement();

		Map<String, String> testClasses = new HashMap<>();
		for (var testDefinitionElement : testRunElement.element("TestDefinitions").elements()) {
			testClasses.put(
					testDefinitionElement.attributeValue("id"), 
					testDefinitionElement.element("TestMethod").attributeValue("className").replace('+', '.'));
		}
		
		Map<String, List<TestCaseData>> testCaseDatum = new LinkedHashMap<>();
		for (var testResultElement: testRunElement.element("Results").elements()) {
			var testId = testResultElement.attributeValue("testId");
			
			var testCaseData = new TestCaseData();
			testCaseData.name = testResultElement.attributeValue("testName");
			testCaseData.duration = parseDuration(testResultElement.attributeValue("duration"));
			testCaseData.statusText = testResultElement.attributeValue("outcome"); 
			Status testStatus;
			switch (testCaseData.statusText) {
				case "Passed":
					testStatus = Status.PASSED;
					break;
				case "Error":
				case "Failed":
				case "Timeout":
				case "Aborted":
					testStatus = Status.NOT_PASSED;
					break;
				case "NotExecuted":
				case "NotRunnable":
				case "Pending":
					testStatus = Status.NOT_RUN;
					break;
				default: 
					testStatus = Status.OTHER;
			}
			testCaseData.status = testStatus;
			
			var detailInfo = new StringBuilder();
			var outputElement = testResultElement.element("Output");
			if (outputElement != null) {
				var errorInfoElement = outputElement.element("ErrorInfo");
				if (errorInfoElement != null) {
					appendMessage(detailInfo, null, errorInfoElement.elementText("Message"));
					appendMessage(detailInfo, "StackTrace", errorInfoElement.elementText("StackTrace"));
				}
				appendMessage(detailInfo, "StdOut", outputElement.elementText("StdOut"));
				appendMessage(detailInfo, "StdErr", outputElement.elementText("StdErr"));
				appendMessage(detailInfo, "Exception", outputElement.elementText("Exception"));
			}
			testCaseData.detailInfo = StringUtils.trimToNull(detailInfo.toString());
			
			var testClass = testClasses.get(testId);
			if (testClass != null) 
				testCaseDatum.computeIfAbsent(testClass, it -> new ArrayList<>()).add(testCaseData);
		}

		var searchManager = OneDev.getInstance(CodeSearchManager.class);
		for (var entry: testCaseDatum.entrySet()) {
			Status status = getOverallStatus(entry.getValue().stream().map(it->it.status).collect(toSet()));
			var duration = entry.getValue().stream().mapToLong(it -> it.duration).sum();
			var symbolHit = searchManager.findPrimarySymbol(build.getProject(), build.getCommitId(), entry.getKey(), ".");
			var blobPath = symbolHit != null? symbolHit.getBlobPath(): null;
			var position = symbolHit != null? symbolHit.getHitPos(): null;
			var testSuite = new TestSuite(entry.getKey(), status, duration, blobPath, position) {

				@Override
				protected Component renderDetail(String componentId, Build build) {
					return null;
				}
			};
			for (var testCaseData: entry.getValue()) {
				var name = testCaseData.name;
				if (name.startsWith(testSuite.getName() + "."))
					name = name.substring(testSuite.getName().length() + 1);
				
				var detailInfo = testCaseData.detailInfo;
				testCases.add(new TestCase(testSuite, name, testCaseData.status, testCaseData.statusText, 
						testCaseData.duration) {
					
					@Override
					protected Component renderDetail(String componentId, Build build) {
						if (detailInfo != null) {
							if (SecurityUtils.canReadCode(build.getProject())) {
								String transformed = new StringTransformer(PATTERN_LOCATION) {

									@Override
									protected String transformUnmatched(String string) {
										return escapeHtml5(string);
									}

									@Override
									protected String transformMatched(Matcher matcher) {
										String file = matcher.group(1);
										int line = Integer.parseInt(matcher.group(2));

										var blobPath = build.getBlobPath(file);
										if (blobPath != null) {
											ProjectBlobPage.State state = new ProjectBlobPage.State();
											state.blobIdent = new BlobIdent(build.getCommitHash(), blobPath);
											PlanarRange range = new PlanarRange(line-1, -1, line-1, -1);
											state.position = BlobRenderer.getSourcePosition(range);
											PageParameters params = ProjectBlobPage.paramsOf(build.getProject(), state);
											String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
											return String.format(" in <a href='%s' target='_blank'>%s:line %d</a>)", url, escapeHtml5(blobPath), line);
										} else {
											return " in " + escapeHtml5(file) + ":line " + line + " ";
										}
									}

								}.transform(detailInfo);

								return new Label(componentId, transformed).setEscapeModelStrings(false);
							} else {
								return new Label(componentId, detailInfo);
							}
						} else {
							return null;
						}
					}
				});
			}
		}
		
		return testCases;
	}
	
	private static void appendMessage(StringBuilder message, String appendType, @Nullable String appendMessage) {
		if (appendMessage != null) {
			if (message.length() != 0)
				message.append("\n\n***** " + appendType + " *****:\n");
			message.append(appendMessage);
		}
	}
	
	private static long parseDuration(String duration) {
		var hmsIterator = Splitter.on(':').split(substringBefore(duration, ".")).iterator();
		var millis = (long) (Double.parseDouble("0." + substringAfter(duration, ".")) * 1000L);
		var hours = Integer.parseInt(hmsIterator.next());
		var minutes = Integer.parseInt(hmsIterator.next());
		var seconds = Integer.parseInt(hmsIterator.next());
		return (hours * 3600 + minutes * 60 + seconds) * 1000L + millis;
	}
	
	private static class TestCaseData {
		String name;
		
		long duration;
		
		Status status;
		
		String statusText;
		
		String detailInfo;
	}
}
