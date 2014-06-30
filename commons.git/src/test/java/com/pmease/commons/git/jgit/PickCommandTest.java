package com.pmease.commons.git.jgit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import com.pmease.commons.git.GitUtils;

public class PickCommandTest extends AbstractGitTest {

	@Test
	public void testPickIfThereAreNoConflicts() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\nline2\nline3\nline4\nline5\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "line2\nline3\nline4\nline5\n", "d1");
		addFileAndCommit("file", "line2\nline3\nline4\nline5\nline6\n", "d2");

		call(workGit.checkout().setName("master"));
		addFileAndCommit("file", "line1\nline2\nmaster3\nline4\nline5\n", "m1");

		Repository repo = workGit.getRepository();
		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		AnyObjectId pickResult = call(pick);
		assertNotNull(pickResult);
		
		assertEquals(
				"line2\nmaster3\nline4\nline5\nline6\n", 
				new String(GitUtils.readFile(repo, pickResult, "file")));
	}

	@Test
	public void testPickIfThereAreConflicts() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\nline2\nline3\nline4\nline5\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "line2\ndev3\nline4\nline5\n", "d1");
		addFileAndCommit("file", "line2\nline3\nline4\nline5\nline6\n", "d2");

		call(workGit.checkout().setName("master"));
		addFileAndCommit("file", "line1\nline2\nmaster3\nline4\nline5\n", "m1");

		Repository repo = workGit.getRepository();
		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		assertNull(call(pick));
	}

	@Test
	public void testPickIfThereAreConflictMerges() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "dev1\n", "d1");
		call(workGit.checkout().setName("master"));
		addFileAndCommit("file", "master1\n", "m1");

		Repository repo = workGit.getRepository();

		call(workGit.checkout().setName("dev"));
		call(workGit.merge().setCommit(true).include(repo.resolve("master")).setStrategy(MergeStrategy.OURS));
		
		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		assertNull(call(pick));
	}

	@Test
	public void testPickIfThereAreNonConflictMerges() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\nline2\nline3\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "dev1\nline2\nline3\n", "d1");
		call(workGit.checkout().setName("master"));
		addFileAndCommit("file", "line1\nline2\nmaster3\n", "m1");

		Repository repo = workGit.getRepository();

		call(workGit.checkout().setName("dev"));
		call(workGit.merge().setCommit(true).include(repo.resolve("master")));
		
		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		AnyObjectId pickResult = call(pick);

		assertArrayEquals(
				GitUtils.readFile(repo, pickResult, "file"), 
				GitUtils.readFile(repo, repo.resolve("dev"), "file"));
	}

	@Test
	public void testPickIfCanBeFastForwarded() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "line1\nline2\n", "d1");
		addFileAndCommit("file", "line1\nline2\nline3\n", "d2");

		Repository repo = workGit.getRepository();

		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		AnyObjectId pickResult = call(pick);

		assertEquals(repo.resolve("dev"), pickResult);
	}

	@Test
	public void testPickIfAlreadyMerged() throws IOException, RefNotFoundException {
		addFileAndCommit("file", "line1\n", "0");
		call(workGit.checkout().setCreateBranch(true).setName("dev"));
		addFileAndCommit("file", "line1\nline2\n", "d1");
		addFileAndCommit("file", "line1\nline2\nline3\n", "d2");

		Repository repo = workGit.getRepository();

		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("master"));
		pick.setTo(repo.resolve("dev"));
		pick.setCommitter(getDefaultCommitter());
		
		AnyObjectId pickResult = call(pick);

		assertEquals(repo.resolve("dev"), pickResult);
	}

	@Test
	public void testPickIfThereAreMultipleMerges() throws IOException, RefNotFoundException {
		Repository repo = workGit.getRepository();

		addFileAndCommit("file", "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\n", "0");
		addFileAndCommit("file", "master1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\n", "m1");
		addFileAndCommit("file", "master1\nmaster2\nline3\nline4\nline5\nline6\nline7\nline8\n", "m2");
		
		call(workGit.checkout().setStartPoint("master~2").setName("dev").setCreateBranch(true));
		addFileAndCommit("file", "line1\nline2\nline3\nline4\nline5\nline6\nline7\ndev8\n", "d1");
		call(workGit.merge().include(repo.resolve("master~1")));
		addFileAndCommit("file", "master1\nline2\nline3\nline4\nline5\nline6\ndev7\ndev8\n", "d2");
		call(workGit.merge().include(repo.resolve("master")));

		PickCommand pick = new PickCommand(repo);
		pick.setFrom(repo.resolve("dev"));
		pick.setTo(repo.resolve("master"));
		pick.setCommitter(getDefaultCommitter());
		
		AnyObjectId pickResult = call(pick);

		assertArrayEquals(
				GitUtils.readFile(repo, pickResult, "file"), 
				GitUtils.readFile(repo, repo.resolve("dev"), "file"));
		List<RevCommit> commits = new ArrayList<>();
		for (RevCommit commit: call(workGit.log().addRange(repo.resolve("master"), pickResult)))
			commits.add(commit);
		
		assertEquals(2, commits.size());
		assertEquals("d2", commits.get(0).getFullMessage());
		assertEquals("d1", commits.get(1).getFullMessage());
	}

}
