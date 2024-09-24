package io.onedev.server.plugin.report.clover;

import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.CoverageStatus;
import io.onedev.server.model.Build;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CloverReportParserTest {

	@Test
	public void testParse() {
		try (var is = Resources.getResource(CloverReportParserTest.class, "coverage-result1.xml").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			SAXReader reader = new SAXReader();
			var report = CloverReportParser.parse(build, reader.read(is), new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			assertEquals(12, report.getStats().getOverallCoverage().getTotalLines());
			assertEquals(2, report.getStats().getOverallCoverage().getCoveredLines());
			assertEquals(12, report.getStats().getGroupCoverages().iterator().next().getTotalLines());
			assertEquals(2, report.getStats().getGroupCoverages().iterator().next().getCoveredLines());
			assertEquals(1, report.getStats().getGroupCoverages().size());
			assertEquals(3, report.getStats().getGroupCoverages().iterator().next().getFileCoverages().size());
			assertEquals(CoverageStatus.COVERED, report.getStatuses().get("src/App.js").get(4));
		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}

		try (var is = Resources.getResource(CloverReportParserTest.class, "coverage-result2.xml").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			SAXReader reader = new SAXReader();
			var report = CloverReportParser.parse(build, reader.read(is), new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			assertEquals(3, report.getStats().getGroupCoverages().size());
			assertEquals(22, report.getStats().getOverallCoverage().getTotalLines());
		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}
		
	}
}