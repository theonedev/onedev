package com.pmease.commons.git.command;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.pmease.commons.git.AbstractGitTest;

public class ListChangedFilesCommandTest extends AbstractGitTest {

	@Test
	public void shouldListChangedFiles() {
		addFileAndCommit("a", "", "commit");
		addFileAndCommit("b", "", "commit");
		addFileAndCommit("c", "", "commit");
		addFileAndCommit("d", "", "commit");
		addFileAndCommit("a", "a", "commit");
		
		Collection<String> changedFiles = git.listChangedFiles("HEAD~4", "HEAD", null);
		
		Assert.assertTrue(changedFiles.containsAll(Sets.newHashSet("a", "b", "c", "d")));
	}

}
