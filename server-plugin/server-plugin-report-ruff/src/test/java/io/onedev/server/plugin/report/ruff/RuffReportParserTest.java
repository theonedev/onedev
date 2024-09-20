package io.onedev.server.plugin.report.ruff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.model.Build;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class RuffReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(RuffReportParserTest.class, "ruff-result.json").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			build.setCommitHash(ObjectId.zeroId().name());
			ObjectMapper mapper = new ObjectMapper();
			var problems = RuffReportParser.parse(build, mapper.readTree(is), new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			var problem = problems.get(0);
			var target = (BlobTarget) problem.getTarget();
			assertEquals(185, target.getLocation().getFromRow());
			assertEquals(0, target.getLocation().getFromColumn());
			assertEquals(185, target.getLocation().getToRow());
			assertEquals(26, target.getLocation().getToColumn());
			
			problem = problems.get(problems.size()-1);
			target = (BlobTarget) problem.getTarget();
			assertEquals(24, target.getLocation().getFromRow());
			assertEquals(0, target.getLocation().getFromColumn());
			assertEquals(28, target.getLocation().getToRow());
			assertEquals(1, target.getLocation().getToColumn());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}