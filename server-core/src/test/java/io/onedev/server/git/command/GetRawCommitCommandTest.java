package io.onedev.server.git.command;

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.onedev.server.git.AbstractGitTest;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.util.GpgUtils;

public class GetRawCommitCommandTest extends AbstractGitTest {

	@Test
	public void test() throws Exception {
		PGPKeyRingGenerator ringGenerator = GpgUtils.generateKeyRingGenerator("noreply@onedev.io");
		PGPSecretKeyRing signingKey = ringGenerator.generateSecretKeyRing();
		
		addFileAndCommit("initial", "", "initial");
		
		String refName = "refs/heads/master";
		ObjectId oldCommitId = git.getRepository().resolve(refName);

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put("readme.md", 
				new BlobContent.Immutable("hello".getBytes(), FileMode.REGULAR_FILE));
		BlobEdits edits = new BlobEdits(Sets.newHashSet(), newBlobs);
		ObjectId newCommitId = edits.commit(git.getRepository(), refName, oldCommitId, oldCommitId, 
				user, "\n\nhello\r\n  world\r\n\r\n", signingKey);
		
		byte[] raw = new GetRawCommitCommand(git.getRepository().getDirectory(), new HashMap<>())
				.revision("master").call();
		
		try (RevWalk revWalk = new RevWalk(git.getRepository())) {
			RevCommit newCommit = revWalk.parseCommit(newCommitId);
			assertArrayEquals(newCommit.getRawBuffer(), raw);
		}
	}
	
}
