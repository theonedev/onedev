package com.pmease.gitplex.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.concurrent.PrioritizedCallable;
import com.pmease.commons.util.concurrent.PrioritizedRunnable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.IndexResult;
import com.pmease.gitplex.core.manager.SequentialWorkManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.WorkManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.SymbolQuery;
import com.pmease.gitplex.search.query.TextQuery;

public class IndexAndSearchTest extends AbstractGitTest {

	private File indexDir;
	
	private Depot depot;
	
	private StorageManager storageManager;
	
	private Extractors extractors;
	
	private IndexManager indexManager;
	
	private SearchManager searchManager;
	
	private UnitOfWork unitOfWork = new UnitOfWork() {

		@Override
		public void begin() {
		}

		@Override
		public void end() {
		}

		@Override
		public <T> T call(Callable<T> callable) {
			try {
				return callable.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void asyncCall(Runnable runnable) {
			runnable.run();
		}

		@Override
		public Session getSession() {
			return null;
		}

		@Override
		public SessionFactory getSessionFactory() {
			return null;
		}
		
	};
	
	private WorkManager workManager = new WorkManager() {

		@Override
		public <T> Future<T> submit(PrioritizedCallable<T> task) {
			try {
				return new CompletedFuture<T>(task.call());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Future<?> submit(PrioritizedRunnable task) {
			task.run();
			return new CompletedFuture<Void>(null);
		}

		@Override
		public <T> Future<T> submit(PrioritizedRunnable task, T result) {
			task.run();
			return new CompletedFuture<T>(result);
		}

		@Override
		public void execute(PrioritizedRunnable command) {
			command.run();
		}

		@Override
		public boolean remove(Runnable task) {
			return false;
		}
		
	};
	
	private SequentialWorkManager sequentialWorkManager = new SequentialWorkManager() {

		@Override
		public <T> Future<T> submit(String key, PrioritizedCallable<T> task) {
			try {
				return new CompletedFuture<T>(task.call());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Future<?> submit(String key, PrioritizedRunnable task) {
			task.run();
			return new CompletedFuture<Void>(null);
		}

		@Override
		public <T> Future<T> submit(String key, PrioritizedRunnable task, T result) {
			task.run();
			return new CompletedFuture<T>(result);
		}

		@Override
		public void execute(String key, PrioritizedRunnable command) {
			command.run();
		}

		@Override
		public void removeExecutor(String key) {
		}

		@Override
		public boolean remove(String key, Runnable task) {
			return false;
		}
		
	};
	
	private Dao dao;
	
	@Override
	protected void setup() {
		super.setup();

		indexDir = FileUtils.createTempDir();
		
		depot = new Depot();
		depot.setId(1L);
		depot.setName("test");
		depot.setAccount(new Account());
		depot.getAccount().setName("test");
        
		storageManager = mock(StorageManager.class);
		when(storageManager.getIndexDir(Mockito.any(Depot.class))).thenReturn(indexDir);
		when(storageManager.getDepotDir(Mockito.any(Depot.class))).thenReturn(new File(git.depotDir(), ".git"));
		
		dao = mock(Dao.class);
		when(dao.load(Depot.class, 1L)).thenReturn(depot);
		
		when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
		
		extractors = mock(Extractors.class);
		when(extractors.getVersion()).thenReturn("java:1");
		when(extractors.getExtractor(anyString())).thenReturn(new JavaExtractor());

		when(AppLoader.getInstance(Extractors.class)).thenReturn(extractors);
		
		searchManager = new DefaultSearchManager(storageManager);
		
		indexManager = new DefaultIndexManager(Sets.<IndexListener>newHashSet(searchManager), 
				storageManager, workManager, sequentialWorkManager, extractors, unitOfWork, dao);
	}

	@Test
	public void testBasic() throws InterruptedException, ExecutionException {
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
		
		ObjectId commit = ObjectId.fromString(git.parseRevision("master", true));
		assertEquals(2, indexManager.index(depot, commit).get().getIndexed());
		
		BlobQuery query = new TextQuery("public", false, false, false, null, null, Integer.MAX_VALUE);
		List<QueryHit> hits = searchManager.search(depot, commit, query);
		assertEquals(4, hits.size());

		query = new SymbolQuery("nam*", false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("nam", false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(0, hits.size());
		
		query = new SymbolQuery("name", false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("cat", true, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(1, hits.size());
		
		code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "  public int age;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog age");		

		commit = ObjectId.fromString(git.parseRevision("master", true));
		assertEquals(1, indexManager.index(depot, commit).get().getIndexed());

		query = new TextQuery("strin", false, false, false, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
		
		query = new SymbolQuery("Age", false, false, null, null, Integer.MAX_VALUE);
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
		
		commit = ObjectId.fromString(git.parseRevision("master", true));
		
		IndexResult indexResult = indexManager.index(depot, commit).get();
		assertEquals(2, indexResult.getChecked());
		assertEquals(1, indexResult.getIndexed());
		
		commit = ObjectId.fromString(git.parseRevision("master~2", true));
		assertEquals(0, indexManager.index(depot, commit).get().getChecked());
		
		when(extractors.getVersion()).thenReturn("java:2");
		when(extractors.getExtractor(anyString())).thenReturn(new Extractor() {

			@Override
			public List<Symbol> extract(String text) throws ExtractException {
				return Lists.<Symbol>newArrayList(new Symbol(null, "tiger", new TokenPosition(0, null)) {

					private static final long serialVersionUID = 1L;

					@Override
					public Component render(String componentId) {
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
		
		assertEquals(2, indexManager.index(depot, commit).get().getIndexed());
		
		query = new SymbolQuery("tiger", false, true, null, null, Integer.MAX_VALUE);
		hits = searchManager.search(depot, commit, query);
		assertEquals(2, hits.size());
	}
	
	@Test
	public void testRegex() throws InterruptedException {
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
		
		ObjectId commit = ObjectId.fromString(git.parseRevision("master", true));
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
	public void testPath() throws InterruptedException {
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
		
		ObjectId commit = ObjectId.fromString(git.parseRevision("master", true));
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
		super.teardown();
		indexManager.onDepotDelete(depot);
	}

}
