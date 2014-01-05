package com.pmease.gitop.web.util;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.pmease.gitop.web.page.project.source.commit.DiffCommand;
import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader.AnnotatedLine;
import com.pmease.gitop.web.page.project.source.commit.patch.Patch;

public class PatchTest {

	@Test public void testPatch() {
		DiffCommand cmd = new DiffCommand(new File("/Users/zhenyu/temp/aaa"));
		cmd.fromRev("9477cf0^").toRev("9477cf0");
		Patch patch = cmd.call();
		
		List<? extends FileHeader> files = patch.getFiles();
		for (FileHeader each : files) {
			List<? extends HunkHeader> hunks = each.getHunks();
			for (HunkHeader hunk : hunks) {
				List<AnnotatedLine> lines = hunk.getAnnotatedLines();
				for (AnnotatedLine line : lines) {
					System.out.println(line);
				}
				System.out.println(StringUtils.repeat('=', 72));
			}
			
			System.out.println(StringUtils.repeat('*', 72));
			System.out.println();
			System.out.println();
		}
	}
}
