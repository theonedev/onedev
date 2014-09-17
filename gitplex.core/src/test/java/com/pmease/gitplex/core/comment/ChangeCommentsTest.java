package com.pmease.gitplex.core.comment;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.BlobText;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.InlineInfo;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestComment;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;

public class ChangeCommentsTest extends AbstractGitTest {

	@SuppressWarnings("serial")
	@Test
	public void test() {
		final List<String> fileRevs = new ArrayList<>();
		fileRevs.add(""
				+ "11\n" //comment1
				+ "22\n" 
				+ "33\n"
				+ "44\n"
				+ "55\n" //comment3, comment6
				+ "66\n"
				+ "77\n"
				+ "88\n" //comment4
				+ "99\n");
		fileRevs.add(""
				+ "111\n"
				+ "222\n"
				+ "33\n"
				+ "44\n"
				+ "55\n"
				+ "66\n"
				+ "77\n"
				+ "88\n"
				+ "99\n");
		fileRevs.add(""
				+ "111\n"
				+ "222\n"
				+ "33\n"
				+ "44\n"
				+ "55\n"
				+ "66\n"
				+ "77\n"
				+ "888\n"
				+ "999\n");
		fileRevs.add(""
				+ "111\n"
				+ "222\n" //comment2
				+ "33\n"
				+ "44\n" //comment5
				+ "555\n"
				+ "66\n"
				+ "77\n"
				+ "8888\n"
				+ "999\n"); //comment8, comment9
		
		addFileAndCommit("file1", fileRevs.get(0), "add file1");
		
		addFileAndCommit("file1", fileRevs.get(1), "modify file1");
		
		addFileAndCommit("file1", fileRevs.get(2), "modify file1");
		
		addFileAndCommit("file1", fileRevs.get(3), "modify file1");
		
		final List<Commit> commits = new ArrayList<>();
		commits.add(git.showRevision("master~3"));
		commits.add(git.showRevision("master~2"));
		commits.add(git.showRevision("master~1"));
		commits.add(git.showRevision("master"));
		
		RevAwareChange change = new RevAwareChange(Change.Status.MODIFIED, "file1", "file1", 
				0, 0, commits.get(0).getHash(), commits.get(3).getHash());


		PullRequest request = new PullRequest();
		request.setTarget(new Branch());
		request.getTarget().setRepository(new Repository() {

			@Override
			public BlobText getBlobText(BlobInfo blobInfo) {
				List<String> commitHashes = new ArrayList<>();
				for (Commit each: commits) 
					commitHashes.add(each.getHash());
			
				int index = commitHashes.indexOf(blobInfo.getRevision());
				String content = fileRevs.get(index);
				try {
					return new BlobText(IOUtils.readLines(new ByteArrayInputStream(content.getBytes())));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
		Collection<PullRequestComment> comments = new ArrayList<>();
		PullRequestComment comment = new PullRequestComment();
		comment.setId(1L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(0).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(0);
		comment.setContent("comment1");
		comments.add(comment);
		
		comment = new PullRequestComment();
		comment.setId(2L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(1).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(1);
		comment.setContent("comment2");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(3L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(1).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(4);
		comment.setContent("comment3");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(4L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(1).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(7);
		comment.setContent("comment4");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(5L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(2).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(3);
		comment.setContent("comment5");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(6L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(2).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(4);
		comment.setContent("comment6");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(7L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(2).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(7);
		comment.setContent("comment7");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(8L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(2).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(8);
		comment.setContent("comment8");
		comments.add(comment);

		comment = new PullRequestComment();
		comment.setId(9L);
		comment.setInlineInfo(new InlineInfo());
		comment.getInlineInfo().setCommit(commits.get(3).getHash());
		comment.getInlineInfo().setFile("file1");
		comment.getInlineInfo().setLine(8);
		comment.setContent("comment9");
		comments.add(comment);

		request.setComments(comments);
		
		request.setBaseCommit(commits.get(0).getHash());
		Collection<PullRequestUpdate> updates = new ArrayList<>();
		
		PullRequestUpdate update = new PullRequestUpdate();
		update.setId(1L);
		update.setHeadCommit(commits.get(1).getHash());
		updates.add(update);
		
		update = new PullRequestUpdate();
		update.setId(2L);
		update.setHeadCommit(commits.get(2).getHash());
		updates.add(update);
		
		update = new PullRequestUpdate();
		update.setId(3L);
		update.setHeadCommit(commits.get(3).getHash());
		updates.add(update);

		request.setUpdates(updates);
		
		ChangeComments changeComments = new ChangeComments(request, change);
		
		assertEquals("comment1", changeComments.getOldComments().get(0).get(0).getContent()); 
		assertEquals("comment2", changeComments.getNewComments().get(1).get(0).getContent()); 
		assertEquals("comment3", changeComments.getOldComments().get(4).get(0).getContent()); 
		assertEquals("comment4", changeComments.getOldComments().get(7).get(0).getContent());
		assertEquals("comment5", changeComments.getNewComments().get(3).get(0).getContent());
		assertEquals("comment6", changeComments.getOldComments().get(4).get(1).getContent()); 
		assertEquals("comment8", changeComments.getNewComments().get(8).get(0).getContent());
		assertEquals("comment9", changeComments.getNewComments().get(8).get(1).getContent()); 
	}

}
