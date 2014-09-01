package com.pmease.gitplex.core.comment;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.BlobInfo;
import com.pmease.commons.git.Change;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.gitplex.core.model.CommentPosition;
import com.pmease.gitplex.core.model.CommitComment;

public class CommentAwareChangeTest extends AbstractGitTest {

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
		
		CommentLoader commentLoader = new CommentLoader() {

			@Override
			public List<CommitComment> loadComments(String commit) {
				List<CommitComment> comments = new ArrayList<>();
				
				List<String> commitHashes = new ArrayList<>();
				for (Commit each: commits) 
					commitHashes.add(each.getHash());
				
				int index = commitHashes.indexOf(commit);
				if (index == 0) {
					CommitComment comment = new CommitComment();
					comment.setId(1L);
					comment.setCommit(commit);
					comment.setContent("comment1");
					comment.setPosition(new CommentPosition("file1", 0));
					comments.add(comment);
				} else if (index == 1) {
					CommitComment comment = new CommitComment();
					comment.setId(2L);
					comment.setCommit(commit);
					comment.setContent("comment2");
					comment.setPosition(new CommentPosition("file1", 1));
					comments.add(comment);

					comment = new CommitComment();
					comment.setId(3L);
					comment.setCommit(commit);
					comment.setContent("comment3");
					comment.setPosition(new CommentPosition("file1", 4));
					comments.add(comment);
					
					comment = new CommitComment();
					comment.setId(4L);
					comment.setCommit(commit);
					comment.setContent("comment4");
					comment.setPosition(new CommentPosition("file1", 7));
					comments.add(comment);
				} else if (index == 2) {
					CommitComment comment = new CommitComment();
					comment.setId(5L);
					comment.setCommit(commit);
					comment.setContent("comment5");
					comment.setPosition(new CommentPosition("file1", 3));
					comments.add(comment);

					comment = new CommitComment();
					comment.setId(6L);
					comment.setCommit(commit);
					comment.setContent("comment6");
					comment.setPosition(new CommentPosition("file1", 4));
					comments.add(comment);

					comment = new CommitComment();
					comment.setId(7L);
					comment.setCommit(commit);
					comment.setContent("comment7");
					comment.setPosition(new CommentPosition("file1", 7));
					comments.add(comment);
					
					comment = new CommitComment();
					comment.setId(8L);
					comment.setCommit(commit);
					comment.setContent("comment8");
					comment.setPosition(new CommentPosition("file1", 8));
					comments.add(comment);
				} else if (index == 3) {
					CommitComment comment = new CommitComment();
					comment.setId(9L);
					comment.setCommit(commit);
					comment.setContent("comment9");
					comment.setPosition(new CommentPosition("file1", 8));
					comments.add(comment);
				} 
				return comments;
			}
			
		};
		
		BlobLoader fileLoader = new BlobLoader() {

			@Override
			public List<String> loadBlob(BlobInfo blobInfo) {
				List<String> commitHashes = new ArrayList<>();
				for (Commit each: commits) 
					commitHashes.add(each.getHash());
			
				int index = commitHashes.indexOf(blobInfo.getRevision());
				String content = fileRevs.get(index);
				try {
					return IOUtils.readLines(new ByteArrayInputStream(content.getBytes()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		};

		RevAwareChange change = new RevAwareChange(Change.Status.MODIFIED, "file1", "file1", 
				0, 0, commits.get(0).getHash(), commits.get(3).getHash());
		
		List<String> commitHashes = new ArrayList<>();
		for (Commit each: commits) 
			commitHashes.add(each.getHash());
		CommentAwareChange commentAwareChange = new CommentAwareChange(change, commitHashes, 
				commentLoader, fileLoader, null);
		
		assertEquals("comment1", commentAwareChange.getOldComments().get(0).get(0).getContent()); 
		assertEquals("comment2", commentAwareChange.getNewComments().get(1).get(0).getContent()); 
		assertEquals("comment3", commentAwareChange.getOldComments().get(4).get(0).getContent()); 
		assertEquals("comment4", commentAwareChange.getOldComments().get(7).get(0).getContent());
		assertEquals("comment5", commentAwareChange.getNewComments().get(3).get(0).getContent());
		assertEquals("comment6", commentAwareChange.getOldComments().get(4).get(1).getContent()); 
		assertEquals("comment8", commentAwareChange.getNewComments().get(8).get(0).getContent());
		assertEquals("comment9", commentAwareChange.getNewComments().get(8).get(1).getContent()); 
	}

}
