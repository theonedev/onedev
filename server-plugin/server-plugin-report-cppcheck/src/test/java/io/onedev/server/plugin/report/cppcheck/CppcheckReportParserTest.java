package io.onedev.server.plugin.report.cppcheck;

import com.google.common.io.Resources;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.model.Build;
import io.onedev.server.util.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CppcheckReportParserTest {

	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(CppcheckReportParserTest.class, "check-result.xml").openStream()) {
			Build build = new Build() {
				@Override
				public String getBlobPath(String filePath) {
					return filePath;
				}
			};
			build.setCommitHash(ObjectId.zeroId().name());
			SAXReader reader = new SAXReader();
			XmlUtils.disallowDocTypeDecl(reader);
			Document doc = reader.read(is);
			
			var problems = CppcheckReportParser.parse(build, doc, new TaskLogger() {
				@Override
				public void log(String message, @Nullable String sessionId) {
				}
			});
			
			assertEquals(8, problems.size());
			
			assertEquals(CodeProblem.Severity.HIGH, problems.get(0).getSeverity());
			assertEquals("6.1-6.2-1", ((BlobTarget)problems.get(0).getTarget()).getLocation().toString());
			assertEquals(CodeProblem.Severity.LOW, problems.get(4).getSeverity());
			assertEquals("0.0-0.0-1", ((BlobTarget)problems.get(4).getTarget()).getLocation().toString());
			assertTrue(problems.get(7).getMessage().contains("tests/src/unit-regression2.cpp, line:210, column:1"));
		} catch (IOException | DocumentException e) {
			throw new RuntimeException(e);
		}
	}

}