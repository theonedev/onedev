package io.onedev.server.plugin.report.trx;

import com.google.common.io.Resources;
import io.onedev.commons.jsymbol.Symbol;
import io.onedev.commons.jsymbol.java.symbols.TypeSymbol;
import io.onedev.commons.loader.AppLoader;
import io.onedev.commons.loader.AppLoaderMocker;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.plugin.report.unittest.UnitTestReport;
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

public class TRXReportParserTest extends AppLoaderMocker {
	@Test
	public void testParse() {
		try (InputStream is = Resources.getResource(TRXReportParserTest.class, "test.trx").openStream()) {
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
			UnitTestReport report = new UnitTestReport(TRXReportParser.parse(build, reader.read(is)), true);

			assertEquals(4, report.getTestSuites().size());
			assertEquals(3, report.getTestCases(null, null, Sets.newSet(UnitTestReport.Status.PASSED)).size());
			assertEquals(2, report.getTestCases(null, null, Sets.newSet(UnitTestReport.Status.NOT_PASSED)).size());

		} catch (IOException | DocumentException e) {
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