package com.pmease.gitplex.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.wicket.Component;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.lang.ExtractException;
import com.pmease.commons.lang.Extractor;
import com.pmease.commons.lang.Extractors;
import com.pmease.commons.lang.Symbol;
import com.pmease.commons.lang.java.JavaExtractor;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.events.RepositoryRemoved;
import com.pmease.gitplex.core.manager.IndexManager;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.query.ContentQuery;
import com.pmease.gitplex.search.query.SymbolQuery;

public class IndexAndSearchTest extends AbstractGitTest {

	private File indexDir;
	
	private Repository repository;
	
	private StorageManager storageManager;
	
	private Extractors extractors;
	
	private IndexManager indexManager;
	
	private SearchManager searchManager;
	
	private EventBus eventBus = new EventBus();
	
	@Override
	protected void setup() {
		super.setup();

//		indexDir = FileUtils.createTempDir();
		
		indexDir = new File("w:\\temp\\index");
		
		repository = mock(Repository.class);
        when(repository.git()).thenReturn(new Git(new File(git.repoDir(), ".git")));
        
		storageManager = mock(StorageManager.class);
		when(storageManager.getIndexDir(Mockito.any(Repository.class))).thenReturn(indexDir);
		
		extractors = mock(Extractors.class);
		when(extractors.getVersion()).thenReturn("java:1");
		when(extractors.getExtractor(anyString())).thenReturn(new JavaExtractor());

		Mockito.when(AppLoader.getInstance(Extractors.class)).thenReturn(extractors);
		
		searchManager = new DefaultSearchManager(storageManager);
		
		indexManager = new DefaultIndexManager(eventBus, 
				Sets.<IndexListener>newHashSet(searchManager), storageManager, extractors);
	}

	@Test
	public void test() {
		String code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog");
		
		code = ""
				+ "public class Cat {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("Cat.java", code, "add cat");
		
		String commitHash = git.parseRevision("master", true);
		assertEquals(2, indexManager.index(repository, commitHash).getIndexed());
		
		ContentQuery contentQuery = new ContentQuery("public", false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, contentQuery);
		assertEquals(4, contentQuery.getHits().size());

		SymbolQuery symbolQuery = new SymbolQuery("nam", false, false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, symbolQuery);
		assertEquals(2, symbolQuery.getHits().size());
		
		symbolQuery = new SymbolQuery("name", true, false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, symbolQuery);
		assertEquals(2, symbolQuery.getHits().size());
		
		code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "  public int age;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog age");		

		commitHash = git.parseRevision("master", true);
		assertEquals(1, indexManager.index(repository, commitHash).getIndexed());

		contentQuery = new ContentQuery("strin", false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, contentQuery);
		assertEquals(2, contentQuery.getHits().size());
		
		symbolQuery = new SymbolQuery("Age", true, false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, symbolQuery);
		assertEquals(1, symbolQuery.getHits().size());
		
		code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "remove dog age");
		
		code = ""
				+ "public class Cat {\n"
				+ "  public String name;\n"
				+ "  public int age;\n"
				+ "}";
		addFileAndCommit("Cat.java", code, "add cat age");
		
		commitHash = git.parseRevision("master", true);
		
		IndexResult indexResult = indexManager.index(repository, commitHash);
		assertEquals(2, indexResult.getChecked());
		assertEquals(1, indexResult.getIndexed());
		
		commitHash = git.parseRevision("master~2", true);
		assertEquals(0, indexManager.index(repository, commitHash).getChecked());
		
		when(extractors.getVersion()).thenReturn("java:2");
		when(extractors.getExtractor(anyString())).thenReturn(new Extractor() {

			@Override
			public Symbol extract(String text) throws ExtractException {
				Symbol symbol = new Symbol() {

					@Override
					public Component render(String componentId) {
						return null;
					}
					
				};
				symbol.name = "tiger";
				return symbol;
			}

			@Override
			public boolean accept(String fileName) {
				return true;
			}

			@Override
			public int getVersion() {
				return 2;
			}
			
		});
		
		assertEquals(2, indexManager.index(repository, commitHash).getIndexed());
		
		symbolQuery = new SymbolQuery("tiger", true, false, Integer.MAX_VALUE);
		searchManager.search(repository, commitHash, symbolQuery);
		assertEquals(2, symbolQuery.getHits().size());
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		eventBus.post(new RepositoryRemoved(repository));
	}

}
