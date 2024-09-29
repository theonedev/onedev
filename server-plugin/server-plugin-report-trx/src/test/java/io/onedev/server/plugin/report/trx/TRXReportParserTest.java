package io.onedev.server.plugin.report.trx;

import com.google.common.io.Resources;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TRXReportParserTest {
	@Test
	public void testParse() {
		try (var is = Resources.getResource(TRXReportParserTest.class, "test.trx").openStream()) {
			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(TRXReportParser.parse(reader.read(is)), true);

			assertEquals(4, report.getTestSuites().size());
			assertEquals(3, report.getTestCases(null, null, Sets.newSet(UnitTestReport.Status.PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(UnitTestReport.Status.NOT_PASSED)).size());
		} catch (IOException | DocumentException e) {
			throw new RuntimeException(e);
		}
	}
	
}