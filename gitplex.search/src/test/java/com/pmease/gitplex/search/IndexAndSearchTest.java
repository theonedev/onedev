package com.pmease.gitplex.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.extractors.ExtractException;
import com.pmease.commons.lang.extractors.Extractor;
import com.pmease.commons.lang.extractors.Extractors;
import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.commons.lang.extractors.java.JavaExtractor;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Range;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.event.depot.DepotDeleted;
import com.pmease.gitplex.core.manager.BatchWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.support.IndexResult;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;

public class IndexAndSearchTest extends AbstractGitTest {

	private File indexDir;
	
	private Depot depot;
	
	private StorageManager storageManager;
	
	private Extractors extractors;
	
	private DefaultIndexManager indexManager;
	
	private DefaultSearchManager searchManager;
	
	@Override
	protected void setup() {
		super.setup();

		indexDir = FileUtils.createTempDir();
		
		depot = new Depot() {

			private static final long serialVersionUID = 1L;

			@Override
			public File getDirectory() {
				return git.getRepository().getDirectory();
			}

			@Override
			public Repository getRepository() {
				return git.getRepository();
			}
			
		};
		depot.setId(1L);
		depot.setName("test");
		depot.setAccount(new Account());
		depot.getAccount().setName("test");
        
		storageManager = mock(StorageManager.class);
		when(storageManager.getIndexDir(Mockito.any(Depot.class))).thenReturn(indexDir);
		
		when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
		
		extractors = mock(Extractors.class);
		when(extractors.getVersion()).thenReturn("java:1");
		when(extractors.getExtractor(anyString())).thenReturn(new JavaExtractor());

		when(AppLoader.getInstance(Extractors.class)).thenReturn(extractors);
		
		searchManager = new DefaultSearchManager(storageManager);
		
		ListenerRegistry listenerRegistry = new ListenerRegistry() {
			
			@Override
			public void post(Object event) {
				searchManager.on((CommitIndexed) event);
			}
			
		};
		
		indexManager = new DefaultIndexManager(listenerRegistry, storageManager, 
				mock(BatchWorkManager.class), extractors, 
				mock(UnitOfWork.class), mock(Dao.class), searchManager);
	}

	@Test
	public void testBasic() throws Exception {
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
		
		ObjectId commit = git.getRepository().resolve("master");
		assertEquals(2, indexManager.index(depot, commit).getIndexed());
		
		BlobQuery query = new TextQuery("public", false, false, false, null, null, Integer.MAX_VALUE);
		List<QueryHit> hits = searchManager.search(depot, commit, query);
		assertEquals(4, hits.size());

		query = new SymbolQuery("nam*", null, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("nam", null, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(0, hits.size());
		
		query = new SymbolQuery("name", null, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("cat", null, true, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(1, hits.size());
		
		code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "  public int age;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog age");		

		commit = git.getRepository().resolve("master");
		assertEquals(1, indexManager.index(depot, commit).getIndexed());

		query = new TextQuery("strin", false, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("Age", null, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(1, hits.size());
		
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
		
		commit = git.getRepository().resolve("master");
		
		IndexResult indexResult = indexManager.index(depot, commit);
		assertEquals(2, indexResult.getChecked());
		assertEquals(1, indexResult.getIndexed());
		
		commit = git.getRepository().resolve("master~2");
		assertEquals(0, indexManager.index(depot, commit).getChecked());
		
		when(extractors.getVersion()).thenReturn("java:2");
		when(extractors.getExtractor(anyString())).thenReturn(new Extractor() {

			@Override
			public List<Symbol> extract(String text) throws ExtractException {
				return Lists.<Symbol>newArrayList(new Symbol(null, "tiger", new TokenPosition(0, null)) {

					private static final long serialVersionUID = 1L;

					@Override
					public Component render(String componentId, Range matchRange) {
						throw new UnsupportedOperationException();
					}

					@Override
					public String describe(List<Symbol> symbols) {
						throw new UnsupportedOperationException();
					}

					@Override
					public ResourceReference getIcon() {
						throw new UnsupportedOperationException();
					}

					@Override
					public String getScope() {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean isPrimary() {
						return false;
					}
					
				});
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
		
		assertEquals(2, indexManager.index(depot, commit).getIndexed());
		
		query = new SymbolQuery("tiger", null, false, true, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
	}
	
	@Test
	public void testRegex() throws Exception {
		String code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "  public String nam;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog");
		
		code = ""
				+ "public class Cat {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("Cat.java", code, "add cat");
		
		ObjectId commit = git.getRepository().resolve("master");
		indexManager.index(depot, commit);

		BlobQuery query = new TextQuery("public|}", true, false, false, null, null, Integer.MAX_VALUE);
		List<QueryHit> hits = searchManager.search(depot, commit, query);
		assertEquals(7, hits.size());
		
		query = new TextQuery("nam", true, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(3, hits.size());
		
		query = new TextQuery("nam", true, false, true, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(1, hits.size());
	}
	
	@Test
	public void testPath() throws Exception {
		String code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("animals/Dog.c", code, "add dog");
		
		code = ""
				+ "public class Cat {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("aminals/Cat.c", code, "add cat");
		
		code = ""
				+ "public class Pine {\n"
				+ "  public String name;\n"
				+ "}";
		addFileAndCommit("plants/Pine.java", code, "add pine");
		
		ObjectId commit = git.getRepository().resolve("master");
		indexManager.index(depot, commit);

		BlobQuery query = new TextQuery("name", false, true, false, "plants/", null, Integer.MAX_VALUE);
		List<QueryHit> hits = searchManager.search(depot, commit, query);
		assertEquals(1, hits.size());
		
		query = new TextQuery("name", false, true, false, null, "*.c", Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
	}
	
	@Override
	protected void teardown() {
		indexManager.on(new DepotDeleted(depot));
		super.teardown();
	}

}
