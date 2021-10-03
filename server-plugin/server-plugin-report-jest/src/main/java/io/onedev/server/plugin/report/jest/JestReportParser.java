package io.onedev.server.plugin.report.jest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.unbescape.html.HtmlEscape;

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
import io.onedev.server.web.page.project.blob.render.BlobRendererer;

public class JestReportParser {

	private static final Pattern PATTERN_LOCATION = Pattern.compile("\\((.*):(\\d+):(\\d+)\\)", Pattern.MULTILINE); 
	
	public static List<TestCase> parse(Build build, JsonNode rootNode) {
		List<TestCase> testCases = new ArrayList<>();
		for (JsonNode testSuiteNode: rootNode.get("testResults")) { 
			String name = testSuiteNode.get("name").asText();
			if (build.getJobWorkspace() != null && name.startsWith(build.getJobWorkspace()))
				name = name.substring(build.getJobWorkspace().length()+1);
			
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
				status = Status.SKIPPED;
				break;
			case "todo":
				status = Status.TODO;
				break;
			case "failed":
				status = Status.FAILED;
				break;
			default:
				if (!testCaseDatum.isEmpty()) {
					if (testCaseDatum.stream().allMatch(it->it.status == Status.TODO))
						status = Status.TODO;
					else if (testCaseDatum.stream().allMatch(it->it.status == Status.SKIPPED))
						status = Status.SKIPPED;
					else
						status = Status.PASSED;
				} else {
					status = Status.PASSED;
				}
			}
			
			TestSuite testSuite = new TestSuite(name, status, duration, message, name) {

				private static final long serialVersionUID = 1L;

				@Override
				protected Component renderMessage(String componentId, Build build) {
					return JestReportParser.renderMessage(componentId, build, getMessage());
				}
				
			};
			
			for (TestCaseData testCaseData: testCaseDatum) {
				testCases.add(new TestCase(testSuite, testCaseData.name, testCaseData.status, 0, testCaseData.message) {

					private static final long serialVersionUID = 1L;

					@Override
					protected Component renderMessage(String componentId, Build build) {
						return JestReportParser.renderMessage(componentId, build, getMessage());
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
						return HtmlEscape.escapeHtml5(string);
					}
		
					@Override
					protected String transformMatched(Matcher matcher) {
						String file = matcher.group(1);
						int line = Integer.parseInt(matcher.group(2));
						int col = Integer.parseInt(matcher.group(3));
						
						if (build.getJobWorkspace() != null && file.startsWith(build.getJobWorkspace())) 
							file = file.substring(build.getJobWorkspace().length()+1);
						BlobIdent blobIdent = new BlobIdent(build.getCommitHash(), file, FileMode.REGULAR_FILE.getBits());
						if (build.getProject().getBlob(blobIdent, false) != null) {
							ProjectBlobPage.State state = new ProjectBlobPage.State();
							state.blobIdent = blobIdent;
							PlanarRange range = new PlanarRange(line-1, col-1, line-1, col); 
							state.position = BlobRendererer.getSourcePosition(range);
							PageParameters params = ProjectBlobPage.paramsOf(build.getProject(), state);
							String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
							return String.format("(<a href='%s'>%s:%d:%d</a>)", url, HtmlEscape.escapeHtml5(file), line, col);
						} else {
							return "(" + HtmlEscape.escapeHtml5(file) + ":" + line + ":" + col + ")";
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
			testCaseData.status = Status.SKIPPED;
			break;
		case "todo":
			testCaseData.status = Status.TODO;
			break;
		default:
			testCaseData.status = Status.FAILED;
		}

		return testCaseData;
	}
	
	private static class TestCaseData {
		
		String name;
		
		Status status;
		
		String message;
		
	}
	
}
