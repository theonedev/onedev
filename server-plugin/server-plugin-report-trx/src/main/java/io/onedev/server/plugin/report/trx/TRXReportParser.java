package io.onedev.server.plugin.report.trx;

import com.google.common.base.Splitter;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.SymbolQuery;
import io.onedev.server.search.code.query.SymbolQueryOption;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.plugin.report.unittest.UnitTestReport.Status.getOverallStatus;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.*;

public class TRXReportParser {

	public static List<TestCase> parse(Build build, Document doc) {
		List<TestCase> testCases = new ArrayList<>();
		Element testRunElement = doc.getRootElement();

		Map<String, String> testClasses = new HashMap<>();
		for (var testDefinitionElement : testRunElement.element("TestDefinitions").elements()) {
			testClasses.put(
					testDefinitionElement.attributeValue("id"), 
					testDefinitionElement.element("TestMethod").attributeValue("className"));
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
			
			var message = new StringBuilder();
			var outputElement = testResultElement.element("Output");
			if (outputElement != null) {
				var errorInfoElement = outputElement.element("ErrorInfo");
				if (errorInfoElement != null) {
					appendMessage(message, null, errorInfoElement.elementText("Message"));
					appendMessage(message, "StackTrace", errorInfoElement.elementText("StackTrace"));
				}
				appendMessage(message, "StdOut", outputElement.elementText("StdOut"));
				appendMessage(message, "StdErr", outputElement.elementText("StdErr"));
				appendMessage(message, "Exception", outputElement.elementText("Exception"));
			}
			testCaseData.message = StringUtils.trimToNull(message.toString());
			
			var testClass = testClasses.get(testId);
			if (testClass != null) 
				testCaseDatum.computeIfAbsent(testClass, it -> new ArrayList<>()).add(testCaseData);
		}

		var searchManager = OneDev.getInstance(CodeSearchManager.class);
		for (var entry: testCaseDatum.entrySet()) {
			Status status = getOverallStatus(entry.getValue().stream().map(it->it.status).collect(toSet()));
			var duration = entry.getValue().stream().mapToLong(it -> it.duration).sum();
			var queryOption = new SymbolQueryOption(substringAfterLast(entry.getKey(), "."), 
					true, null);
			var query = new SymbolQuery.Builder(queryOption).primary(true).count(10).build();
			String blobPath = null;
			for (var hit: searchManager.search(build.getProject(), build.getCommitId(), query)) {
				SymbolHit symbolHit = (SymbolHit) hit;
				if (entry.getKey().equals(symbolHit.getSymbol().getFQN())) {
					if (blobPath == null)
						blobPath = symbolHit.getBlobPath();
					else 
						blobPath = null;
				}
			}
			var testSuite = new TestSuite(entry.getKey(), status, duration, null, blobPath);
			for (var testCaseData: entry.getValue()) {
				var name = testCaseData.name;
				if (name.startsWith(testSuite.getName() + "."))
					name = name.substring(testSuite.getName().length() + 1);
				testCases.add(new TestCase(testSuite, name, testCaseData.status, testCaseData.statusText, 
						testCaseData.duration, testCaseData.message));
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
		
		String message;
	}
}
