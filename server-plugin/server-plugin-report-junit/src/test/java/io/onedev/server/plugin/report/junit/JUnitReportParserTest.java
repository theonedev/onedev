package io.onedev.server.plugin.report.junit;

import com.google.common.io.Resources;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JUnitReportParserTest {

	@Test
	public void testParse() {
		try (var is = Resources.getResource(JUnitReportParserTest.class, "test-result.xml").openStream()) {
			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(JUnitReportParser.parse(reader.read(is)), true);
			
			assertEquals(1, report.getTestSuites().size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.NOT_RUN)).size());
			
		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}

		try (var is = Resources.getResource(JUnitReportParserTest.class, "test-result2.xml").openStream()) {
			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(JUnitReportParser.parse(reader.read(is)), true);

			assertEquals(2, report.getTestSuites().size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.PASSED)).size());
			assertEquals(4, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.NOT_RUN)).size());

		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}
	}

}
