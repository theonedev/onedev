package com.pmease.gitplex.search;

import static com.pmease.gitplex.search.FieldConstants.BLOB_DEFS_SYMBOLS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Throwables;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.Analyzers;
import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Outline;
import com.pmease.commons.lang.c.CAnalyzer;
import com.pmease.commons.lang.java.JavaAnalyzer;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

public class DefaultIndexManagerTest extends AbstractGitTest {

	private File indexDir;
	
	private Repository repository;
	
	private StorageManager storageManager;
	
	private Analyzers analyzers;
	
	private IndexManager indexManager;
	
	@Override
	protected void setup() {
		super.setup();

		indexDir = FileUtils.createTempDir();
		
		repository = mock(Repository.class);
        when(repository.git()).thenReturn(git);
        
		storageManager = mock(StorageManager.class);
		when(storageManager.getIndexDir(Mockito.any(Repository.class))).thenReturn(indexDir);
		
		analyzers = mock(Analyzers.class);
		when(analyzers.getVersion()).thenReturn("java:1.0");
		when(analyzers.getVersion(anyString())).thenReturn("1.0");
		when(analyzers.analyze(anyString(), anyString())).thenAnswer(new Answer<AnalyzeResult>() {

			@Override
			public AnalyzeResult answer(InvocationOnMock invocation) throws Throwable {
				String fileContent = (String) invocation.getArguments()[0];
				return new JavaAnalyzer().analyze(fileContent);
			}
			
		});
		
		indexManager = new DefaultIndexManager(storageManager, analyzers);
	}

	@Test
	public void test() {
		String code = ""
				+ "public class Hello {"
				+ "  public String message;"
				+ "}";
		addFileAndCommit("Hello.java", code, "1");
		
		code = ""
				+ "public class World {"
				+ "  public String message;"
				+ "}";
		addFileAndCommit("World.java", code, "2");
		
		String commitHash = git.parseRevision("master", true);
		indexManager.index(repository, commitHash);
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_DEFS_SYMBOLS.query("message"), Integer.MAX_VALUE);
				assertEquals(2, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		code = ""
				+ "public class Hello2 {"
				+ "  public String message;"
				+ "}";
		addFileAndCommit("Hello.java", code, "3");		

		commitHash = git.parseRevision("master", true);
		indexManager.index(repository, commitHash);
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_DEFS_SYMBOLS.query("message"), Integer.MAX_VALUE);
				assertEquals(3, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		when(analyzers.getVersion()).thenReturn("java:2.0");
		when(analyzers.getVersion(anyString())).thenReturn("2.0");
		final List<LangToken> symbols = new ArrayList<>();
		symbols.add(new LangToken(0, "value", 0, 0));
		AnalyzeResult analyzeResult = new AnalyzeResult(symbols, new Outline() {

			@Override
			public List<LangToken> getSymbols() {
				return symbols;
			}
			
		});
		when(analyzers.analyze(anyString(), anyString())).thenReturn(analyzeResult);
		
		indexManager.index(repository, commitHash);
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_DEFS_SYMBOLS.query("value"), Integer.MAX_VALUE);
				assertEquals(2, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Test
	public void test2() {
		Git linuxGit = new Git(new File("w:\\linux\\.git"));
		when(repository.git()).thenReturn(linuxGit);
		when(storageManager.getIndexDir(Mockito.any(Repository.class))).thenReturn(new File("w:\\temp\\index"));
		
		when(analyzers.analyze(anyString(), anyString())).thenAnswer(new Answer<AnalyzeResult>() {

			@Override
			public AnalyzeResult answer(InvocationOnMock invocation) throws Throwable {
				String fileContent = (String) invocation.getArguments()[0];
				return new CAnalyzer().analyze(fileContent);
			}
			
		});
		
		int count = 0;
		for (Commit commit: linuxGit.log("master~1000", "master", null, 0, 0)) {
			System.out.println(++count);
			indexManager.index(repository, commit.getHash());			
		}
	}
	
	@Override
	protected void teardown() {
		FileUtils.deleteDir(indexDir);
		
		super.teardown();
	}

}
