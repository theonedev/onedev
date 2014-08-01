package com.pmease.commons.git.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;

public class IsBinaryCommandTest extends AbstractGitTest {

	@Test
	public void test() throws IOException {
		addFileAndCommit("file", "hello world", "initial commit");

		byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		OutputStream os = new FileOutputStream(new File(git.repoDir(), "file"));
		os.write(bytes);
		os.close();
		
		git.add("file").commit("second commit", false, false);
		
		assertFalse(git.isBinary("file", "master~1"));
		assertTrue(git.isBinary("file", "master"));
	}

}
