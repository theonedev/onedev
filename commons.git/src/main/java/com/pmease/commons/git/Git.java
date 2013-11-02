package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.git.command.AddCommand;
import com.pmease.commons.git.command.ListTreeCommand;
import com.pmease.commons.git.command.CalcMergeBaseCommand;
import com.pmease.commons.git.command.CheckAncestorCommand;
import com.pmease.commons.git.command.CheckoutCommand;
import com.pmease.commons.git.command.CommitCommand;
import com.pmease.commons.git.command.DeleteRefCommand;
import com.pmease.commons.git.command.GetCommitCommand;
import com.pmease.commons.git.command.InitCommand;
import com.pmease.commons.git.command.ListBranchesCommand;
import com.pmease.commons.git.command.ListChangedFilesCommand;
import com.pmease.commons.git.command.ListTagsCommand;
import com.pmease.commons.git.command.MergeCommand;
import com.pmease.commons.git.command.UpdateRefCommand;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.GeneralException;

@SuppressWarnings("serial")
public class Git implements Serializable {

	private final File repoDir;

	public Git(File repoDir) {
		this.repoDir = repoDir;

		if (!repoDir.exists())
		    FileUtils.createDir(repoDir);
	}

	public File repoDir() {
		return repoDir;
	}

	public void createBranch(String branchName, String commitHash) {
		if (new ListBranchesCommand(repoDir).call().contains(branchName))
			throw new GeneralException("Branch %s already exists.", branchName);

		new UpdateRefCommand(repoDir).refName("refs/heads/" + branchName).revision(commitHash)
				.call();
	}

	public void deleteBranch(String branchName) {
		new DeleteRefCommand(repoDir).refName("refs/heads/" + branchName).call();
	}

	public void createTag(String tagName, String commitHash) {
		if (new ListTagsCommand(repoDir).call().contains(tagName))
			throw new GeneralException("Tag %s already exists.", tagName);

		new UpdateRefCommand(repoDir).refName("refs/tags/" + tagName).revision(commitHash).call();
	}

	public void deleteTag(String tagName) {
		new DeleteRefCommand(repoDir).refName("refs/tags/" + tagName).call();
	}

	public Commit getCommit(String revision) {
		return new GetCommitCommand(repoDir).revision(revision).call();
	}

	public List<TreeNode> listTree(String revision, @Nullable String path, boolean recursive) {
		return new ListTreeCommand(repoDir).revision(revision).path(path).recursive(recursive).call();
	}

	public void init(boolean bare) {
		new InitCommand(repoDir).bare(bare).call();
	}

	public void add(String path) {
		new AddCommand(repoDir).addPath(path).call();
	}

	public void commit(String message, boolean amend) {
		new CommitCommand(repoDir).message(message).amend(amend).call();
	}

	public Collection<String> listChangedFiles(String fromRev, String toRev) {
		return new ListChangedFilesCommand(repoDir).fromRev(fromRev).toRev(toRev).call();
	}

	public void checkout(String revision, boolean newBranch) {
		new CheckoutCommand(repoDir).revision(revision).newBranch(newBranch).call();
	}

	public void updateRef(String refName, String revision, 
			@Nullable String oldRevision, @Nullable String reason) {
		new UpdateRefCommand(repoDir).refName(refName).revision(revision).oldRevision(oldRevision)
				.reason(reason).call();
	}
	
	public void deleteRef(String refName) {
		new DeleteRefCommand(repoDir).refName(refName).call();
	}

	public void merge(String revision) {
		new MergeCommand(repoDir).revision(revision).call();
	}

	public String calcMergeBase(String rev1, String rev2) {
		return new CalcMergeBaseCommand(repoDir).rev1(rev1).rev2(rev2).call();
	}
	
	public boolean checkAncestor(String ancestor, String descendant) {
		return new CheckAncestorCommand(repoDir).ancestor(ancestor).descendant(descendant).call();
	}
	
	public Collection<String> listBranches() {
		return new ListBranchesCommand(repoDir).call();
	}
	
	public Collection<String> listTags() {
		return new ListTagsCommand(repoDir).call();
	}
}
