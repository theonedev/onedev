package com.pmease.gitplex.search;

import static com.pmease.gitplex.search.FieldConstants.BLOB_SYMBOLS;
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
import com.google.common.eventbus.EventBus;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.lang.AnalyzeResult;
import com.pmease.commons.lang.Analyzers;
import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Outline;
import com.pmease.commons.lang.java.JavaAnalyzer;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.manager.IndexResult;
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
		
		indexManager = new DefaultIndexManager(new EventBus(), storageManager, analyzers);
	}

	@Test
	public void test() {
		String code = ""
				+ "public class Dog {"
				+ "  public String name;"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog");
		
		code = ""
				+ "public class Cat {"
				+ "  public String name;"
				+ "}";
		addFileAndCommit("Cat.java", code, "add cat");
		
		String commitHash = git.parseRevision("master", true);
		assertEquals(2, indexManager.index(repository, commitHash).getIndexed());
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_SYMBOLS.query("name"), Integer.MAX_VALUE);
				assertEquals(2, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		code = ""
				+ "public class Dog {"
				+ "  public String name;"
				+ "  public int age;"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog age");		

		commitHash = git.parseRevision("master", true);
		assertEquals(1, indexManager.index(repository, commitHash).getIndexed());
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_SYMBOLS.query("name"), Integer.MAX_VALUE);
				assertEquals(3, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		code = ""
				+ "public class Dog {"
				+ "  public String name;"
				+ "}";
		addFileAndCommit("Dog.java", code, "remove dog age");
		
		code = ""
				+ "public class Cat {"
				+ "  public String name;"
				+ "  public int age;"
				+ "}";
		addFileAndCommit("Cat.java", code, "add cat age");
		
		commitHash = git.parseRevision("master", true);
		
		IndexResult indexResult = indexManager.index(repository, commitHash);
		assertEquals(2, indexResult.getChecked());
		assertEquals(1, indexResult.getIndexed());
		
		commitHash = git.parseRevision("master~2", true);
		assertEquals(0, indexManager.index(repository, commitHash).getChecked());
		
		when(analyzers.getVersion()).thenReturn("java:2.0");
		when(analyzers.getVersion(anyString())).thenReturn("2.0");
		final List<LangToken> symbols = new ArrayList<>();
		symbols.add(new LangToken(0, "tiger", 0, 0));
		AnalyzeResult analyzeResult = new AnalyzeResult(symbols, new Outline() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<LangToken> getSymbols() {
				return symbols;
			}
			
		});
		when(analyzers.analyze(anyString(), anyString())).thenReturn(analyzeResult);
		
		assertEquals(2, indexManager.index(repository, commitHash).getIndexed());
		
		try (Directory directory = FSDirectory.open(indexDir)) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				TopDocs topDocs = searcher.search(BLOB_SYMBOLS.query("tiger"), Integer.MAX_VALUE);
				assertEquals(2, topDocs.totalHits);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	protected void teardown() {
		FileUtils.deleteDir(indexDir);
		
		super.teardown();
	}

}
