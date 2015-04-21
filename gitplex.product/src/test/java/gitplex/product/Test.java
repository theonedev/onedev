package gitplex.product;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;

import com.pmease.commons.git.AbstractGitTest;

public class Test extends AbstractGitTest {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\mozilla\\.git"));
		int start = 9200;
		AnyObjectId oldId = repo.resolve("master~" + start);
		String dir = "";
		LastCommitsOfChildren oldCommits = new LastCommitsOfChildren(repo, oldId, dir);
		
		final Map<AnyObjectId, Map<String, Value>> cache = new HashMap<>();
		cache.put(oldId, oldCommits);

		for (int i=start-1; i>0; i--) {
			System.out.println("Proessing master~" + i);
			AnyObjectId newId = repo.resolve("master~"+i);
			
			long time = System.currentTimeMillis();
			LastCommitsOfChildren newCommits = new LastCommitsOfChildren(repo, newId, dir, new Cache() {
				
				@Override
				public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
					return cache.get(commitId);
				}
				
			});
			long elapsed = System.currentTimeMillis()-time;
			System.out.println(elapsed);
			if (elapsed > 100) 
				cache.put(newId, newCommits);
			if (elapsed > 4000) { 
				System.out.println(i);
				System.exit(1);
			}
		}
		
		repo.close();
	}
	
	@org.junit.Test
	public void test1() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\mozilla\\.git"));
		String dir = "";
		ObjectId oldId = repo.resolve("test1~1");
		LastCommitsOfChildren oldCommits = new LastCommitsOfChildren(repo, oldId, dir);
		final Map<AnyObjectId, Map<String, Value>> cache = new HashMap<>();
		cache.put(oldId, oldCommits);

		long time = System.currentTimeMillis();
		new LastCommitsOfChildren(repo, repo.resolve("test1"), dir, new Cache() {
			
			@Override
			public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
				return cache.get(commitId);
			}
			
		});
		System.out.println(System.currentTimeMillis()-time);
		
		repo.close();
	}
	
	@org.junit.Test
	public void test2() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\mozilla\\.git"));
		String dir = "";
		ObjectId oldId = repo.resolve("test~1");
		LastCommitsOfChildren oldCommits = new LastCommitsOfChildren(repo, oldId, dir);
		final Map<AnyObjectId, Map<String, Value>> cache = new HashMap<>();
		cache.put(oldId, oldCommits);

		long time = System.currentTimeMillis();
		new LastCommitsOfChildren(repo, repo.resolve("test"), dir, new Cache() {
			
			@Override
			public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
				return cache.get(commitId);
			}
			
		});
		System.out.println(System.currentTimeMillis()-time);
		
		repo.close();
	}
	
	@org.junit.Test
	public void test3() throws IOException, NoHeadException, GitAPIException {
		Repository repo = new FileRepository(new File("w:\\mozilla\\.git"));
		String dir = "";
		String rev = "master";
		new LastCommitsOfChildren(repo, repo.resolve(rev), dir);
		repo.close();
	}
	
}