package org.eclipse.jgit.revwalk;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.junit.Test;

import io.onedev.server.git.AbstractGitTest;

public class LastCommitsOfChildrenTest extends AbstractGitTest {

	@Test
	public void testMergeWithoutTouchingSameFile() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("d", "", "add a file to dev branch");
		git.checkout().setName("master").call();
		addFileAndCommit("m", "", "add a file to master branch");
		git.merge().include(git.getRepository().resolve("dev")).setCommit(true).call();
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("master"));
		assertEquals(git.getRepository().resolve("master~1"), lastCommits.get("m").getId());
		assertEquals(git.getRepository().resolve("dev"), lastCommits.get("d").getId());
		assertEquals(git.getRepository().resolve("master~2"), lastCommits.get("initial").getId());
	}

	@Test
	public void testMergeTouchingSameFile() throws Exception {
		addFileAndCommit("file", "1\n2\n3\n4\n5\n", "initial commit");
		git.checkout().setName("dev").setCreateBranch(true).call();
		addFileAndCommit("file", "0\n1\n2\n3\n4\n5\n", "add first line");
		git.checkout().setName("master").call();
		addFileAndCommit("file", "1\n2\n3\n4\n5\n6\n", "add last line");
		git.merge().include(git.getRepository().resolve("dev")).setCommit(true).call();
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("master"));
		assertEquals(git.getRepository().resolve("master"), lastCommits.get("file").getId());
	}

	@Test
	public void testDirAndFile() throws Exception {
		createDir("dir/dir1/subdir");
		createDir("dir/dir2");
		addFileAndCommit("dir/dir1/file", "", "commit");
		addFileAndCommit("dir/dir2/file", "", "commit");
		addFileAndCommit("dir/dir1/subdir/file", "", "commit");
		addFileAndCommit("dir/file", "", "commit");
		
		LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(git.getRepository(), git.getRepository().resolve("master"), "dir");
		assertEquals(3, lastCommits.size());
		assertEquals(git.getRepository().resolve("master~1"), lastCommits.get("dir1").getId());
		assertEquals(git.getRepository().resolve("master~2"), lastCommits.get("dir2").getId());
		assertEquals(git.getRepository().resolve("master"), lastCommits.get("file").getId());
	}
	
	@Test
	public void testWithCache() throws Exception {
		addFileAndCommit("initial", "", "initial commit");
		git.checkout().setName("feature1").setCreateBranch(true).call();
		addFileAndCommit("feature1", "", "add feature1");
		git.checkout().setName("master").call();
		addFileAndCommit("master", "", "add master");
		git.merge().include(git.getRepository().resolve("feature1")).setCommit(true).call();
		
		final ObjectId oldId = git.getRepository().resolve("master");
		final LastCommitsOfChildren oldLastCommits = new LastCommitsOfChildren(git.getRepository(), oldId);
		Cache cache = new Cache() {

			@Override
			public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
				if (commitId.equals(oldId))
					return oldLastCommits;
				else
					return null;
			}
			
		};
		assertEquals(oldLastCommits, new LastCommitsOfChildren(git.getRepository(), oldId, cache));
		
		git.checkout().setName("feature2").setCreateBranch(true).call();
		addFileAndCommit("initial", "hello", "modify initial");
		git.checkout().setName("master").call();
		git.merge().include(git.getRepository().resolve("feature2")).setCommit(true).call();

		ObjectId newId = git.getRepository().resolve("master");
		assertEquals(new LastCommitsOfChildren(git.getRepository(), newId), new LastCommitsOfChildren(git.getRepository(), newId, cache));
	}
	
}
