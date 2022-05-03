package io.onedev.server.plugin.report.junit;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.search.code.CodeSearchManager;

public class JUnitReportParser {

	public static List<TestCase> parse(Build build, Document doc) {
		List<TestCase> testCases = new ArrayList<>();
		Element testSuiteElement = doc.getRootElement();
		String name = testSuiteElement.attributeValue("name");
		long duration = (long) (Double.parseDouble(testSuiteElement.attributeValue("time"))*1000);
		int tests = Integer.parseInt(testSuiteElement.attributeValue("tests"));
		int failures = Integer.parseInt(testSuiteElement.attributeValue("failures"));
		int errors = Integer.parseInt(testSuiteElement.attributeValue("errors"));
		
		int skipped = 0;
		String skippedString = testSuiteElement.attributeValue("skipped");
		if (StringUtils.isNotBlank(skippedString))
			skipped = Integer.parseInt(skippedString);
		
		Status status;
		if (failures != 0 || errors != 0)
			status = Status.FAILED;
		else if (skipped == tests)
			status = Status.SKIPPED;
		else
			status = Status.PASSED;
		
		String fileName;
		if (name.contains("."))
			fileName = StringUtils.substringAfterLast(name, ".") + ".java";
		else
			fileName = name + ".java";
		
		String partialBlobPath = name.replace('.', '/') + ".java";

		String blobPath = OneDev.getInstance(CodeSearchManager.class)
				.findBlobPath(build.getProject(), build.getCommitId(), fileName, partialBlobPath);
		
		TestSuite testSuite = new TestSuite(name, status, duration, null, blobPath);
		
		for (Element testCaseElement: testSuiteElement.elements("testcase")) {
			name = testCaseElement.attributeValue("name");
			if (testCaseElement.element("skipped") != null) {
				testCases.add(new TestCase(testSuite, name, Status.SKIPPED, 0, null));
			} else {
				duration = (long) (Double.parseDouble(testCaseElement.attributeValue("time"))*1000);
				status = Status.PASSED;
				String message = null;
				Element failureElement = testCaseElement.element("failure");
				Element errorElement = testCaseElement.element("error");
				if (failureElement != null) {
					status = Status.FAILED;
					message = failureElement.getText();
				} else if (errorElement != null) {
					status = Status.FAILED;
					message = errorElement.getText();
				} 
				testCases.add(new TestCase(testSuite, name, status, duration, message));
			}
		}
		return testCases;
	}
	
}
