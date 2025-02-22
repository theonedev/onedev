package io.onedev.server.plugin.report.gtest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jetbrains.annotations.Nullable;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;

public class GTestReportParser {

	public static List<TestCase> parse(Build build, Document doc) {
		List<TestCase> testCases = new ArrayList<>();
		for (Element testSuiteElement : doc.getRootElement().elements()) {
			String name = testSuiteElement.attributeValue("name");
			long duration = (long)(getDouble(testSuiteElement.attributeValue("time"))*1000);
			int tests = getInt(testSuiteElement.attributeValue("tests"));
			int failures =getInt(testSuiteElement.attributeValue("failures"));
			int errors = getInt(testSuiteElement.attributeValue("errors"));

			int skipped = 0;
			String skippedString = testSuiteElement.attributeValue("skipped");
			if (StringUtils.isNotBlank(skippedString))
				skipped = getInt(skippedString);

			int disabled = 0;
			String disabledString = testSuiteElement.attributeValue("disabled");
			if (StringUtils.isNotBlank(disabledString))
				disabled = getInt(disabledString);
			
			Status status;
			if (failures != 0 || errors != 0)
				status = Status.NOT_PASSED;
			else if (skipped + disabled == tests)
				status = Status.NOT_RUN;
			else
				status = Status.PASSED;
			
			TestSuite testSuite = new TestSuite(name, status, duration, null, null) {

				@Nullable
				@Override
				protected Component renderDetail(String componentId, Build build) {
					return null;
				}
			};

			for (Element testCaseElement: testSuiteElement.elements("testcase")) {
				name = testCaseElement.attributeValue("name");
				var result = testCaseElement.attributeValue("result");
				if (result.equals("skipped") || result.equals("suppressed")) {
					var skippedElement = testCaseElement.element("skipped");
					String message = skippedElement != null? skippedElement.getText(): null;
					var blobLocation = parseBlobLocation(build, message);
					testCases.add(new TestCase(testSuite, name, Status.NOT_RUN, result, 0) {

						@Nullable
						@Override
						protected Component renderDetail(String componentId, Build build) {
							return renderMessage(componentId, build, blobLocation, message);
						}
					});
				} else {
					duration = (long)(getDouble(testCaseElement.attributeValue("time"))*1000);
					status = Status.PASSED;
					String message;
					Element failureElement = testCaseElement.element("failure");
					Element errorElement = testCaseElement.element("error");
					if (failureElement != null) {
						status = Status.NOT_PASSED;
						message = failureElement.getText();
					} else if (errorElement != null) {
						status = Status.NOT_PASSED;
						message = errorElement.getText();
					} else {
						message = null;
					}
					
					var blobLocation = parseBlobLocation(build, message);
					testCases.add(new TestCase(testSuite, name, status, null, duration) {

						@Nullable
						@Override
						protected Component renderDetail(String componentId, Build build) {
							return renderMessage(componentId, build, blobLocation, message);
						}
					});
				}
			}
		}
		return testCases;
	}
	
	@Nullable
	private static Component renderMessage(String componentId, Build build, 
										   @Nullable Pair<String, Integer> blobLocation, 
										   @Nullable String message) {
		if (blobLocation != null) {
			ProjectBlobPage.State state = new ProjectBlobPage.State();
			state.blobIdent = new BlobIdent(build.getCommitHash(), blobLocation.getLeft());
			PlanarRange range = new PlanarRange(blobLocation.getRight()-1, -1, blobLocation.getRight()-1, -1);
			state.position = BlobRenderer.getSourcePosition(range);
			PageParameters params = ProjectBlobPage.paramsOf(build.getProject(), state);
			String url = RequestCycle.get().urlFor(ProjectBlobPage.class, params).toString();
			var html = String.format("<a href='%s'>%s</a><br>%s", 
					url, 
					StringUtils.substringBefore(message, "\n").trim(), 
					StringUtils.substringAfter(message, "\n"));
			return new Label(componentId, html).setEscapeModelStrings(false);
		} else if (message != null) {
			return new Label(componentId, message);
		} else {
			return null;
		}
	}
	
	@Nullable
	private static Pair<String, Integer> parseBlobLocation(Build build, @Nullable String message) {
		if (message != null) {
			var firstLine = StringUtils.substringBefore(message, "\n").trim();
			var possibleFilePath = StringUtils.substringBeforeLast(firstLine, ":");
			var possibleLineNumber = StringUtils.substringAfterLast(firstLine, ":");
			var blobPath = build.getBlobPath(possibleFilePath);
			if (blobPath != null && NumberUtils.isDigits(possibleLineNumber))
				return new ImmutablePair<>(blobPath, Integer.parseInt(possibleLineNumber));
		}
		return null;
	}
	
	private static int getInt(String input) {
		if (input == null) 
			return 0;
		return Integer.parseInt(input);
	}

	private static double getDouble(String input) {
		if (input == null) 
			return 0;
		return Double.parseDouble(input);
	}
	
}
