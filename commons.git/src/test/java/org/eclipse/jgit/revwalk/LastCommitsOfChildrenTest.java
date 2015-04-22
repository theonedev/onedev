package org.eclipse.jgit.revwalk;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.junit.Test;

import com.pmease.commons.git.AbstractJGitTest;

public class LastCommitsOfChildrenTest extends AbstractJGitTest {

	@Test
	public void testMergeWithoutTouchingSameFile() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("d", "", "add a file to dev branch");
		git.checkout().setName("master").call();
		addFileAndCommit("m", "", "add a file to master branch");
		git.merge().include(repo.resolve("dev")).setCommit(true).call();
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repo, repo.resolve("master"));
		assertEquals(repo.resolve("master~1"), lastCommits.get("m").getId());
		assertEquals(repo.resolve("dev"), lastCommits.get("d").getId());
		assertEquals(repo.resolve("master~2"), lastCommits.get("initial").getId());
	}

	@Test
	public void testMergeTouchingSameFile() throws Exception {
		addFileAndCommit("file", "1\n2\n3\n4\n5\n", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("file", "0\n1\n2\n3\n4\n5\n", "add first line");
		git.checkout().setName("master").call();
		addFileAndCommit("file", "1\n2\n3\n4\n5\n6\n", "add last line");
		git.merge().include(repo.resolve("dev")).setCommit(true).call();
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repo, repo.resolve("master"));
		assertEquals(repo.resolve("master"), lastCommits.get("file").getId());
	}

	@Test
	public void testDirAndFile() throws Exception {
		createDir("dir/dir1/subdir");
		createDir("dir/dir2");
		addFileAndCommit("dir/dir1/file", "", "commit");
		addFileAndCommit("dir/dir2/file", "", "commit");
		addFileAndCommit("dir/dir1/subdir/file", "", "commit");
		addFileAndCommit("dir/file", "", "commit");
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repo, repo.resolve("master"), "dir");
		assertEquals(3, lastCommits.size());
		assertEquals(repo.resolve("master~1"), lastCommits.get("dir1").getId());
		assertEquals(repo.resolve("master~2"), lastCommits.get("dir2").getId());
		assertEquals(repo.resolve("master"), lastCommits.get("file").getId());
	}
	
	@Test
	public void testWithCache() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("feature1").setCreateBranch(true).call();
		addFileAndCommit("feature1", "", "add feature1");
		git.checkout().setName("master").call();
		addFileAndCommit("master", "", "add master");
		git.merge().include(repo.resolve("feature1")).setCommit(true).call();
		
		final ObjectId oldId = repo.resolve("master");
		final LastCommitsOfChildren oldLastCommits = new LastCommitsOfChildren(repo, oldId);
		Cache cache = new Cache() {

			@Override
			public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
				if (commitId.equals(oldId))
					return oldLastCommits;
				else
					return null;
			}
			
		};
		assertEquals(oldLastCommits, new LastCommitsOfChildren(repo, oldId, cache));
		
		git.checkout().setName("feature2").setCreateBranch(true).call();
		addFileAndCommit("initial", "hello", "modify initial");
		git.checkout().setName("master").call();
		git.merge().include(repo.resolve("feature2")).setCommit(true).call();

		ObjectId newId = repo.resolve("master");
		assertEquals(new LastCommitsOfChildren(repo, newId), new LastCommitsOfChildren(repo, newId, cache));
	}
	
}
