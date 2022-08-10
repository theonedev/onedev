package io.onedev.server.plugin.report.junit;

import java.util.ArrayList;
import java.util.Iterator;
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
		Element rootElement = doc.getRootElement();

		List<Element> rootElements = new ArrayList<>();
		if (rootElement.getName().equals("testsuite")) {
			// JUnit format
			rootElements.add(rootElement);
		} else if (rootElement.getName().equals("testsuites")) {
			// JUnit report format
			Iterator<Element> elements = rootElement.elementIterator("testsuite");
			while(elements.hasNext()) {
				rootElements.add(elements.next());
			}
		} else {
			return testCases;
		}
		
		for (Element testSuiteElement : rootElements) {
			String name = testSuiteElement.attributeValue("name");
			long duration = getDouble(testSuiteElement.attributeValue("time"));
			int tests = getInt(testSuiteElement.attributeValue("tests"));
			int failures =getInt(testSuiteElement.attributeValue("failures"));
			int errors = getInt(testSuiteElement.attributeValue("errors"));

			int skipped = 0;
			String skippedString = testSuiteElement.attributeValue("skipped");
			if (StringUtils.isNotBlank(skippedString))
				skipped = getInt(skippedString);

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
					duration = getDouble(testCaseElement.attributeValue("time"));
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
		}
		return testCases;
	}
	
	public static int getInt(String input) {
		if (input == null) {
			return 0;
		}
		return Integer.parseInt(input);
	}

	public static long getDouble(String input) {
		if (input == null) {
			return 0;
		}
		return (long) Double.parseDouble(input);
	}
	
}
