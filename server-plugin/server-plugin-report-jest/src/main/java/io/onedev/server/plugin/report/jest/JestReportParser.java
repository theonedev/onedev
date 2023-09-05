package io.onedev.server.plugin.report.jest;

import com.fasterxml.jackson.databind.JsonNode;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.StringTransformer;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.unbescape.html.HtmlEscape.escapeHtml5;

public class JestReportParser {

	private static final Pattern PATTERN_LOCATION = Pattern.compile("\\((.*):(\\d+):(\\d+)\\)", Pattern.MULTILINE); 
	
	public static List<TestCase> parse(Build build, JsonNode rootNode) {
		List<TestCase> testCases = new ArrayList<>();
		for (JsonNode testSuiteNode: rootNode.get("testResults")) { 
			String name = testSuiteNode.get("name").asText();
			String blobPath = build.getBlobPath(name);
			
			String message = testSuiteNode.get("message").asText(null);
			if (StringUtils.isBlank(message))
				message = null;
			
			long duration = testSuiteNode.get("endTime").asLong() - testSuiteNode.get("startTime").asLong();
			
			List<TestCaseData> testCaseDatum = new ArrayList<>();
			
			for (JsonNode testCaseNode: testSuiteNode.get("assertionResults")) 
				testCaseDatum.add(parseTestCase(testCaseNode));
			
			Status status;
			switch(testSuiteNode.get("status").asText()) {
				case "pending":
				case "todo":
					status = Status.NOT_RUN;
					break;
				case "failed":
					status = Status.NOT_PASSED;
					break;
				default:
					if (!testCaseDatum.isEmpty()) {
						if (testCaseDatum.stream().allMatch(it -> it.status == Status.NOT_RUN))
							status = Status.NOT_RUN;
						else
							status = Status.PASSED;
					} else {
						status = Status.PASSED;
					}
			}
			
			var testSuiteMessage = message;
			TestSuite testSuite = new TestSuite(blobPath!=null?blobPath:name, status, duration, blobPath, null) {

				private static final long serialVersionUID = 1L;

				@Override
				protected Component renderDetail(String componentId, Build build) {
					return JestReportParser.renderMessage(componentId, build, testSuiteMessage);
				}
				
			};
			
			for (TestCaseData testCaseData: testCaseDatum) {
				var testCaseMessage = testCaseData.message;
				testCases.add(new TestCase(testSuite, testCaseData.name, testCaseData.status, testCaseData.statusText, 0) {

					private static final long serialVersionUID = 1L;

					@Override
					protected Component renderDetail(String componentId, Build build) {
						return JestReportParser.renderMessage(componentId, build, testCaseMessage);
					}
					
				});
			}
		}
		return testCases;
	}

	@Nullable
	private static Component renderMessage(String componentId, Build build, @Nullable String message) {
		if (message != null) {
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
						int col = Integer.parseInt(matcher.group(3));
						
						var blobPath = build.getBlobPath(file);
						if (blobPath != null) {
							ProjectBlobPage.State state = new ProjectBlobPage.State();
							state.blobIdent = new BlobIdent(build.getCommitHash(), blobPath);
							PlanarRange range = new PlanarRange(line-1, col-1, line-1, col); 
							state.position = BlobRenderer.getSourcePosition(range);
							PageParameters params = ProjectBlobPage.paramsOf(build.getProject(), state);
							String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
							return String.format("(<a href='%s'>%s:%d:%d</a>)", url, escapeHtml5(blobPath), line, col);
						} else {
							return "(" + escapeHtml5(file) + ":" + line + ":" + col + ")";
						}
					}
					
				}.transform(message);
				
				return new Label(componentId, transformed).setEscapeModelStrings(false);
			} else {
				return new Label(componentId, message);
			}
		} else {
			return null;
		}
	}
	
	private static TestCaseData parseTestCase(JsonNode rootNode) {
		TestCaseData testCaseData = new TestCaseData();
		
		StringBuilder nameBuilder = new StringBuilder();
		for (JsonNode ancestorTitleNode: rootNode.get("ancestorTitles")) 
			nameBuilder.append(ancestorTitleNode.asText()).append("/");
		nameBuilder.append(rootNode.get("title").asText());
		
		testCaseData.name = nameBuilder.toString();

		List<String> messages = new ArrayList<>();
		for (JsonNode messageNode: rootNode.get("failureMessages")) 
			messages.add(messageNode.asText());
		
		if (!messages.isEmpty())
			testCaseData.message = StringUtils.join(messages, "\n\n");

		switch (rootNode.get("status").asText()) {
			case "passed":
				testCaseData.status = Status.PASSED;
				break;
			case "pending":
				testCaseData.status = Status.NOT_RUN;
				testCaseData.statusText = "pending";
				break;
			case "todo":
				testCaseData.status = Status.NOT_RUN;
				testCaseData.statusText = "todo";
				break;
			default:
				testCaseData.status = Status.NOT_PASSED;
		}

		return testCaseData;
	}
	
	private static class TestCaseData {
		
		String name;
		
		Status status;
		
		String statusText;
		
		String message;
		
	}
	
}
