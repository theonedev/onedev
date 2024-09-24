package io.onedev.server.plugin.report.gtest;

import com.google.common.io.Resources;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class GTestReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(GTestReportParserTest.class, "test-result.xml").openStream()) {
			Build build = new Build();
			build.setCommitHash(ObjectId.zeroId().name());

			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(GTestReportParser.parse(build, reader.read(is)), true);
			
			assertEquals(3, report.getTestSuites().size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.PASSED)).size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).size());
			assertEquals(5, report.getTestCases(null, null, Sets.newSet(Status.NOT_RUN)).size());
			assertEquals(1503, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).iterator().next().getDuration());
		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}		
	}

}
