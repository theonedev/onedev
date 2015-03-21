package com.pmease.gitplex.search;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.wicket.Component;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Throwables;
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
import com.pmease.gitplex.search.hit.QueryHit;
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
		
		searchManager = new DefaultSearchManager(storageManager, new ExecutorService() {

			@Override
			public void execute(Runnable command) {
			}

			@Override
			public void shutdown() {
			}

			@Override
			public List<Runnable> shutdownNow() {
				return null;
			}

			@Override
			public boolean isShutdown() {
				return false;
			}

			@Override
			public boolean isTerminated() {
				return false;
			}

			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
				return false;
			}

			@Override
			public <T> Future<T> submit(Callable<T> task) {
				try {
					final T result = task.call();
					return new Future<T>() {

						@Override
						public boolean cancel(boolean mayInterruptIfRunning) {
							return false;
						}

						@Override
						public boolean isCancelled() {
							return false;
						}

						@Override
						public boolean isDone() {
							return false;
						}

						@Override
						public T get() throws InterruptedException,
								ExecutionException {
							return result;
						}

						@Override
						public T get(long timeout, TimeUnit unit)
								throws InterruptedException,
								ExecutionException, TimeoutException {
							return result;
						}
						
					};
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
				
			}

			@Override
			public <T> Future<T> submit(Runnable task, T result) {
				return null;
			}

			@Override
			public Future<?> submit(Runnable task) {
				return null;
			}

			@Override
			public <T> List<Future<T>> invokeAll(
					Collection<? extends Callable<T>> tasks)
					throws InterruptedException {
				return null;
			}

			@Override
			public <T> List<Future<T>> invokeAll(
					Collection<? extends Callable<T>> tasks, long timeout,
					TimeUnit unit) throws InterruptedException {
				return null;
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
					throws InterruptedException, ExecutionException {
				return null;
			}

			@Override
			public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
					long timeout, TimeUnit unit) throws InterruptedException,
					ExecutionException, TimeoutException {
				return null;
			}
			
		});
		
		indexManager = new DefaultIndexManager(eventBus, 
				Sets.<IndexListener>newHashSet(searchManager), storageManager, extractors);
	}

	@Test
	public void test() throws Exception {
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
		List<QueryHit> hits = searchManager.search(repository, commitHash, contentQuery).get();
		assertEquals(4, hits.size());

		SymbolQuery symbolQuery = new SymbolQuery("nam", false, false, Integer.MAX_VALUE);
		hits = searchManager.search(repository, commitHash, symbolQuery).get();
		assertEquals(2, hits.size());
		
		symbolQuery = new SymbolQuery("name", true, false, Integer.MAX_VALUE);
		hits = searchManager.search(repository, commitHash, symbolQuery).get();
		assertEquals(2, hits.size());
		
		code = ""
				+ "public class Dog {\n"
				+ "  public String name;\n"
				+ "  public int age;\n"
				+ "}";
		addFileAndCommit("Dog.java", code, "add dog age");		

		commitHash = git.parseRevision("master", true);
		assertEquals(1, indexManager.index(repository, commitHash).getIndexed());

		contentQuery = new ContentQuery("strin", false, Integer.MAX_VALUE);
		hits = searchManager.search(repository, commitHash, contentQuery).get();
		assertEquals(2, hits.size());
		
		symbolQuery = new SymbolQuery("Age", true, false, Integer.MAX_VALUE);
		hits = searchManager.search(repository, commitHash, symbolQuery).get();
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
		hits = searchManager.search(repository, commitHash, symbolQuery).get();
		assertEquals(2, hits.size());
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		eventBus.post(new RepositoryRemoved(repository));
	}

}
