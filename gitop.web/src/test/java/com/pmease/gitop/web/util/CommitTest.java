package com.pmease.gitop.web.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gitective.core.CommitUtils;
import org.junit.Test;

public class CommitTest {

	static File gitDir = new File("/Users/zhenyu/data/gitop/projects/3/code");
	
	@Test public void testLog() throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(gitDir);
		Iterable<RevCommit> it = git.log().setMaxCount(25).add(CommitUtils.getCommit(git.getRepository(), "v3.9-rc6"))
			.call();
		
		for (RevCommit each : it) {
			System.out.println(each.getCommitterIdent().getWhen() + "\t" + each.getShortMessage());
		}
	}
}
