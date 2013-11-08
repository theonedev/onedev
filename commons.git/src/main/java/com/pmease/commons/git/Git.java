package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.AddCommand;
import com.pmease.commons.git.command.CalcMergeBaseCommand;
import com.pmease.commons.git.command.CheckAncestorCommand;
import com.pmease.commons.git.command.CheckoutCommand;
import com.pmease.commons.git.command.CloneCommand;
import com.pmease.commons.git.command.CommitCommand;
import com.pmease.commons.git.command.DeleteRefCommand;
import com.pmease.commons.git.command.DiffCommand;
import com.pmease.commons.git.command.InitCommand;
import com.pmease.commons.git.command.ListBranchesCommand;
import com.pmease.commons.git.command.ListChangedFilesCommand;
import com.pmease.commons.git.command.ListTagsCommand;
import com.pmease.commons.git.command.ListTreeCommand;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.MergeCommand;
import com.pmease.commons.git.command.ReadFileCommand;
import com.pmease.commons.git.command.RemoveCommand;
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

	public Git createBranch(String branchName, String commitHash) {
		if (new ListBranchesCommand(repoDir).call().contains(branchName))
			throw new GeneralException("Branch %s already exists.", branchName);

		new UpdateRefCommand(repoDir).refName("refs/heads/" + branchName).revision(commitHash)
				.call();
		
		return this;
	}

	public Git deleteBranch(String branchName) {
		new DeleteRefCommand(repoDir).refName("refs/heads/" + branchName).call();
		return this;
	}

	public Git createTag(String tagName, String commitHash) {
		if (new ListTagsCommand(repoDir).call().contains(tagName))
			throw new GeneralException("Tag %s already exists.", tagName);

		new UpdateRefCommand(repoDir).refName("refs/tags/" + tagName).revision(commitHash).call();
		return this;
	}

	public Git deleteTag(String tagName) {
		new DeleteRefCommand(repoDir).refName("refs/tags/" + tagName).call();
		return this;
	}

	public Commit resolveCommit(String revision) {
		List<Commit> commits = new LogCommand(repoDir).toRev(revision).maxCommits(1).call();
		Preconditions.checkState(commits.size() == 1);
		return commits.get(0);
	}

	public List<TreeNode> listTree(String revision, @Nullable String path, boolean recursive) {
		return new ListTreeCommand(repoDir).revision(revision).path(path).recursive(recursive).call();
	}

	public Git init(boolean bare) {
		new InitCommand(repoDir).bare(bare).call();
		return this;
	}

	public Git add(String path) {
		new AddCommand(repoDir).addPath(path).call();
		return this;
	}
	
	public Git remove(String path) {
		new RemoveCommand(repoDir).removePath(path).call();
		return this;
	}
	
	public Git clone(String from, boolean bare) {
		new CloneCommand(repoDir).from(from).bare(bare).call();
		return this;
	}

	public Git commit(String message, boolean amend) {
		new CommitCommand(repoDir).message(message).amend(amend).call();
		return this;
	}

	public Collection<String> listChangedFiles(String fromRev, String toRev) {
		return new ListChangedFilesCommand(repoDir).fromRev(fromRev).toRev(toRev).call();
	}

	public Git checkout(String revision, boolean newBranch) {
		new CheckoutCommand(repoDir).revision(revision).newBranch(newBranch).call();
		return this;
	}

	public Git updateRef(String refName, String revision, 
			@Nullable String oldRevision, @Nullable String reason) {
		new UpdateRefCommand(repoDir).refName(refName).revision(revision).oldRevision(oldRevision)
				.reason(reason).call();
		return this;
	}
	
	public Git deleteRef(String refName) {
		new DeleteRefCommand(repoDir).refName(refName).call();
		return this;
	}

	public Git merge(String revision) {
		new MergeCommand(repoDir).revision(revision).call();
		return this;
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
	
	public byte[] readFile(String revision, String path) {
		return new ReadFileCommand(repoDir).revision(revision).path(path).call();
	}
	
	public List<Commit> log(@Nullable String fromRev, @Nullable String toRev, 
			@Nullable String path, int maxCommits) {
		return new LogCommand(repoDir).fromRev(fromRev).toRev(toRev)
				.path(path).maxCommits(maxCommits).call();
	}
	
	public Commit retrieveLastCommmit(String revision, @Nullable String path) {
		return log(null, revision, path, 1).get(0);
	}
	
	public List<FileChangeWithDiffs> diff(String fromRev, String toRev, @Nullable String path) {
		return new DiffCommand(repoDir).fromRev(fromRev).toRev(toRev).path(path).call();
	}
	
}
