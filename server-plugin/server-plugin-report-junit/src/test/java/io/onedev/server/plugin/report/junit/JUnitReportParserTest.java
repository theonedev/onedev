package io.onedev.server.plugin.report.junit;

import com.google.common.io.Resources;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.search.code.CodeSearchManager;
import io.onedev.server.search.code.hit.QueryHit;
import io.onedev.server.search.code.hit.SymbolHit;
import io.onedev.server.search.code.query.BlobQuery;
import io.onedev.server.search.code.query.TooGeneralQueryException;
import org.apache.lucene.search.IndexSearcher;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JUnitReportParserTest extends AppLoaderMocker {

	@Test
	public void testJUnit() {
		try (InputStream is = Resources.getResource(JUnitReportParserTest.class, "test-result.xml").openStream()) {
			Mockito.when(AppLoader.getInstance(CodeSearchManager.class)).thenReturn(new CodeSearchManager() {

				@Override
				public List<QueryHit> search(Project project, ObjectId commit, BlobQuery query)
						throws TooGeneralQueryException {
					return null;
				}

				@Override
				public List<Symbol> getSymbols(Project project, ObjectId blobId, String blobPath) {
					return null;
				}

				@Override
				public List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath) {
					return null;
				}

				@Override
				public String findBlobPathBySuffix(Project project, ObjectId commit, String blobPathSuffix) {
					return "Test.java";
				}

				@Nullable
				@Override
				public SymbolHit findPrimarySymbol(Project project, ObjectId commitId, String symbolFQN, String fqnSeparator) {
					return null;
				}

			});
			
			Build build = new Build();
			build.setCommitHash(ObjectId.zeroId().name());

			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(JUnitReportParser.parse(build, reader.read(is)), true);
			
			assertEquals(1, report.getTestSuites().size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).size());
			assertEquals(1, report.getTestCases(null, null, Sets.newSet(Status.NOT_RUN)).size());
			
		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}		
	}
	
	@Test
	public void testJUnitReport() {
		try (InputStream is = Resources.getResource(JUnitReportParserTest.class, "test-results.xml").openStream()) {
			Mockito.when(AppLoader.getInstance(CodeSearchManager.class)).thenReturn(new CodeSearchManager() {

				@Override
				public List<QueryHit> search(Project project, ObjectId commit, BlobQuery query)
						throws TooGeneralQueryException {
					return null;
				}

				@Override
				public List<Symbol> getSymbols(Project project, ObjectId blobId, String blobPath) {
					return null;
				}

				@Override
				public List<Symbol> getSymbols(IndexSearcher searcher, ObjectId blobId, String blobPath) {
					return null;
				}

				@Override
				public String findBlobPathBySuffix(Project project, ObjectId commit, String blobPathSuffix) {
					return "Test.java";
				}

				@Nullable
				@Override
				public SymbolHit findPrimarySymbol(Project project, ObjectId commitId, String symbolFQN, String fqnSeparator) {
					return null;
				}

			});

			Build build = new Build();
			build.setCommitHash(ObjectId.zeroId().name());

			SAXReader reader = new SAXReader();
			UnitTestReport report = new UnitTestReport(JUnitReportParser.parse(build, reader.read(is)), true);

			assertEquals(2, report.getTestSuites().size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.PASSED)).size());
			assertEquals(4, report.getTestCases(null, null, Sets.newSet(Status.NOT_PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(Status.NOT_RUN)).size());

		} catch (IOException|DocumentException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setup() {
	}

	@Override
	protected void teardown() {
	}

}
