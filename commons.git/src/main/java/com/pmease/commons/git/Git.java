package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.AddCommand;
import com.pmease.commons.git.command.AddNoteCommand;
import com.pmease.commons.git.command.AddSubModuleCommand;
import com.pmease.commons.git.command.BlameCommand;
import com.pmease.commons.git.command.CalcMergeBaseCommand;
import com.pmease.commons.git.command.CheckoutCommand;
import com.pmease.commons.git.command.CloneCommand;
import com.pmease.commons.git.command.CommitCommand;
import com.pmease.commons.git.command.DeleteRefCommand;
import com.pmease.commons.git.command.DiffCommand;
import com.pmease.commons.git.command.FetchCommand;
import com.pmease.commons.git.command.InitCommand;
import com.pmease.commons.git.command.IsAncestorCommand;
import com.pmease.commons.git.command.IsBinaryCommand;
import com.pmease.commons.git.command.IsTreeLinkCommand;
import com.pmease.commons.git.command.ListBranchesCommand;
import com.pmease.commons.git.command.ListChangedFilesCommand;
import com.pmease.commons.git.command.ListSubModulesCommand;
import com.pmease.commons.git.command.ListTagsCommand;
import com.pmease.commons.git.command.ListTreeCommand;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.MergeCommand;
import com.pmease.commons.git.command.ParseRevisionCommand;
import com.pmease.commons.git.command.PushCommand;
import com.pmease.commons.git.command.RemoveCommand;
import com.pmease.commons.git.command.ResetCommand;
import com.pmease.commons.git.command.ShowCommand;
import com.pmease.commons.git.command.ShowForConsumeCommand;
import com.pmease.commons.git.command.ShowRefCommand;
import com.pmease.commons.git.command.ShowSymbolicRefCommand;
import com.pmease.commons.git.command.UpdateRefCommand;
import com.pmease.commons.git.command.UpdateSymbolicRefCommand;
import com.pmease.commons.util.GeneralException;
import com.pmease.commons.util.execution.StreamConsumer;

@SuppressWarnings("serial")
public class Git implements Serializable {

	public static final String REFS_HEADS = "refs/heads/";
	
	public static final String REFS_TAGS = "refs/tags/";
	
	private final File repoDir;

	public Git(File repoDir) {
		this.repoDir = repoDir;
	}

	public File repoDir() {
		return repoDir;
	}

	/**
	 * Create branch even in a bare repository.
	 * 
	 * @param branchName
	 * 			name of the branch to create
	 * @param commitHash
	 * 			commit hash of the branch 
	 * @return
	 * 			this git object
	 */
	public Git createBranch(String branchName, String commitHash) {
		if (new ListBranchesCommand(repoDir).call().contains(branchName))
			throw new GeneralException("Branch %s already exists.", branchName);

		new UpdateRefCommand(repoDir).refName(Git.REFS_HEADS + branchName).revision(commitHash)
				.call();
		
		return this;
	}

	/**
	 * Rename specified branch. Default branch will be updated if necessary.
	 * 
	 * @param oldName
	 *			old name of the branch 	
	 * @param newName
	 * 			new name of the branch
	 * @return
	 * 			this git object
	 */
	public Git renameBranch(String oldName, String newName) {
		String commitHash = parseRevision(oldName, true);
		createBranch(newName, commitHash);
		if (resolveDefaultBranch().equals(oldName))
			updateDefaultBranch(newName);
		deleteBranch(oldName);
		
		return this;
	}

	/**
	 * delete branch even in a bare repository.
	 * 
	 * @param branchName
	 * 			name of the branch to delete
	 * @return
	 * 			this git object
	 */
	public Git deleteBranch(String branchName) {
		new DeleteRefCommand(repoDir).refName(REFS_HEADS + branchName).call();
		return this;
	}

	public Git createTag(String tagName, String commitHash) {
		if (new ListTagsCommand(repoDir).call().contains(tagName))
			throw new GeneralException("Tag %s already exists.", tagName);

		new UpdateRefCommand(repoDir).refName(REFS_TAGS + tagName).revision(commitHash).call();
		return this;
	}

	public Git deleteTag(String tagName) {
		new DeleteRefCommand(repoDir).refName(REFS_TAGS + tagName).call();
		return this;
	}

	public Commit showRevision(String revision) {
		List<Commit> commits = new LogCommand(repoDir).toRev(revision).maxCount(1).call();
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

	public Git add(String... paths) {
		new AddCommand(repoDir).addPaths(paths).call();
		return this;
	}
	
	public Git remove(String... paths) {
		new RemoveCommand(repoDir).removePaths(paths).call();
		return this;
	}
	
	public Git clone(String from, boolean bare, boolean shared, boolean noCheckout, @Nullable String branch) {
		new CloneCommand(repoDir).from(from).bare(bare).shared(shared).noCheckout(noCheckout).branch(branch).call();
		return this;
	}

	public Git clone(String from, boolean bare) {
		return clone(from, bare, false, true, null);
	}
	
	public Git reset(@Nullable String mode, @Nullable String commit) {
		new ResetCommand(repoDir).mode(mode).commit(commit).call();
		return this;
	}

	public Git commit(String message, boolean add, boolean amend) {
		new CommitCommand(repoDir).message(message).amend(amend).call();
		return this;
	}

	public Collection<String> listChangedFiles(String fromRev, String toRev) {
		return new ListChangedFilesCommand(repoDir).fromRev(fromRev).toRev(toRev).call();
	}

	public Git checkout(String revision, @Nullable String newBranch) {
		new CheckoutCommand(repoDir).revision(revision).newBranch(newBranch).call();
		return this;
	}

	/**
	 * Update specified symbolic ref name with specified ref name.
	 * 
	 * @param symbolicRefName
	 * 			symbolic ref name to be updated
	 * @param refName
	 * 			ref name to point to
	 * @param reason
	 * 			reason to update the symbolic ref
	 */
	public Git updateSymbolicRef(String symbolicRefName, String refName, @Nullable String reason) {
		new UpdateSymbolicRefCommand(repoDir).symbolicRefName(symbolicRefName).refName(refName)
				.reason(reason).call();
		return this;
	}

	/**
	 * Show ref name for specified symbolic ref name.
	 * 
	 * @param symbolicRefName
	 * 			symbolic ref name to be shown
	 * @return
	 * 			ref name of specified symbolic ref name
	 */
	public String showSymbolicRef(String symbolicRefName) {
		return new ShowSymbolicRefCommand(repoDir).symbolicRefName(symbolicRefName).call();
	}

	/**
	 * Update specified ref with specified revision.
	 * 
	 * @param refName
	 * 			reference to be updated
	 * @param revision
	 * 			new revision of the reference
	 * @param oldRevision
	 * 			optional old revision of the reference
	 * @param reason
	 * 			reason to update the ref
	 * @return
	 * 			<tt>true</tt> if success, or <tt>false</tt> if the reference 
	 * 			can not be locked due to old revision not matching
	 */
	public boolean updateRef(String refName, String revision, 
			@Nullable String oldRevision, @Nullable String reason) {
		return new UpdateRefCommand(repoDir).refName(refName)
				.revision(revision).oldRevision(oldRevision)
				.reason(reason).call();
	}
	
	public Git deleteRef(String refName) {
		new DeleteRefCommand(repoDir).refName(refName).call();
		return this;
	}

	public boolean merge(String revision, @Nullable String strategy, 
			@Nullable String strategyOption, @Nullable String message) {
		return new MergeCommand(repoDir).revision(revision).message(message).call();
	}

	public Git fetch(String from, String refspec) {
		new FetchCommand(repoDir).from(from).refspec(refspec).call();
		return this;
	}
	
	public Git push(String to, String refspec) {
		new PushCommand(repoDir).to(to).refspec(refspec).call();
		return this;
	}

	public List<RefInfo> showRefs(String pattern) {
		return new ShowRefCommand(repoDir).pattern(pattern).call();
	}
	
	/**
	 * Parse commit hash of specified revision. 
	 * 
	 * @param revision
	 * 			revision to be parsed
	 * @param shouldExist
	 * 			indicate whether or not the revision should already exist
	 * @return
	 * 			commit hash of specified revision, or <tt>null</tt> if revision 
	 * 			does not exist, and <tt>shouldExist</tt> is specified as 
	 * 			<tt>false</tt>
	 * @throws
	 * 			IllegalStateException if specified revision does not exist 
	 * 			while <tt>shouldExist<tt> is specified as <tt>true</tt>
	 */
	public @Nullable String parseRevision(String revision, boolean shouldExist) {
		String commit = new ParseRevisionCommand(repoDir).revision(revision).call();
		if (commit == null) {
			if (shouldExist)
				throw new IllegalStateException();
			else
				return null;
		} else {
			return commit;
		}
	}

	public String calcMergeBase(String rev1, String rev2) {
		return new CalcMergeBaseCommand(repoDir).rev1(rev1).rev2(rev2).call();
	}
	
	/**
	 * Check if specified param <tt>ancestor</tt> is ancestor of specified param <tt>descendant</tt>.
	 * 
	 * @param ancestor
	 * 			ancestor to be checked
	 * @param descendant
	 * 			descendant to be checked 
	 * @return
	 * 			<tt>true</tt> if first param is ancestor of second param
	 */
	public boolean isAncestor(String ancestor, String descendant) {
		if (ancestor.equals(descendant))
			return true;
		else
			return new IsAncestorCommand(repoDir).ancestor(ancestor).descendant(descendant).call();
	}
	
	public Collection<String> listBranches() {
		return new ListBranchesCommand(repoDir).call();
	}
	
	public Collection<String> listTags() {
		return new ListTagsCommand(repoDir).call();
	}
	
	/**
	 * Show content of specified path of specified revision.
	 * 
	 * @param revision
	 * 			revision of the path
	 * @param path
	 * 			path to show content
	 * @param consumer
	 * 			consumer to absorb content of specified path and revision
	 *
	 */
	public Git show(String revision, String path, StreamConsumer consumer) {
		new ShowForConsumeCommand(repoDir).revision(revision).path(path).consumer(consumer).call();
		return this;
	}
	
	public byte[] show(String revision, String path) {
		return new ShowCommand(repoDir).revision(revision).path(path).call();
	}

	public List<Commit> log(@Nullable String fromRev, @Nullable String toRev, 
			@Nullable String path, int maxCount, int skip) {
		return new LogCommand(repoDir).fromRev(fromRev).toRev(toRev)
				.path(path).maxCount(maxCount).skip(skip).call();
	}
	
	public Commit retrieveLastCommmit(String revision, @Nullable String path) {
		return log(null, revision, path, 1, 0).get(0);
	}
	
	/**
	 * General diff between specified revisions for specified path.
	 *  
	 * @param fromRev
	 * 			calculate diffs since this revision (not include changes of this revision)
	 * @param toRev
	 * 			calculate diffs till this revision (include changes of this revision)
	 * @param path
	 * 			optionally specify directory or file for diff. Use <tt>null</tt> to diff 
	 * 			all files in the repository
	 * @param contextLines
	 * 			number of not changed lines before and after the difference lines
	 * @return
	 * 			list of file changes with diff information included
	 */
	public List<FileChangeWithDiffs> diff(String fromRev, String toRev, @Nullable String path, int contextLines) {
		return new DiffCommand(repoDir).fromRev(fromRev).toRev(toRev).contextLines(contextLines).path(path).call();
	}

	public Git addNote(String object, String message) {
		new AddNoteCommand(repoDir).object(object).message(message).call();
		return this;
	}
	
	/**
	 * Display commit information for every region of specified file and revision. 
	 * 
	 * @param file
	 * 			file for blame
	 * @param revision
	 * 			revision of the file for blame
	 * @param start
	 * 			specify start line to be included in the blame. Use <tt>-1</tt> 
	 * 			to blame from start of the file
	 * @param end
	 * 			specify end line to be included in the blame. Use <tt>-1</tt> to 
	 * 			blame to the end of file  
	 * @return
	 * 			list of blame objects. The blame object consists of commit information 
	 * 			and lines associated with this commit. All lines of all blame objects
	 * 			consist the whole file. 
	 */
	public List<Blame> blame(String file, String revision, int start, int end) {
		return new BlameCommand(repoDir).file(file).revision(revision).start(start).end(end).call();
	}

	public boolean isBinary(String file, String revision) {
		return new IsBinaryCommand(repoDir).file(file).revision(revision).call();
	}
	
	public boolean isTreeLink(String symlink, String revision) {
		return new IsTreeLinkCommand(repoDir).symlink(symlink).revision(revision).call();
	}
	
	public Git addSubModule(String url, String path) {
		new AddSubModuleCommand(repoDir).url(url).path(path).call();
		return this;
	}
	
	public Map<String, String> listSubModules(String revision) {
		return new ListSubModulesCommand(repoDir).revision(revision).call();
	}

	public String resolveDefaultBranch() {
		String refName = showSymbolicRef("HEAD");
		Preconditions.checkState(refName.startsWith(Git.REFS_HEADS));
		return refName.substring(Git.REFS_HEADS.length());
	}

	public void updateDefaultBranch(String defaultBranch) {
		updateSymbolicRef("HEAD", Git.REFS_HEADS + defaultBranch, null);
	}

	public boolean hasCommits() {
		File headsDir = new File(repoDir, Git.REFS_HEADS);
		return headsDir.exists() && headsDir.list().length != 0 || new File(repoDir, "packed-refs").exists();
	}
	
	public boolean isValid() {
		return new File(repoDir, "objects").exists();
	}
	
	@Override
	public String toString() {
		return repoDir.toString();
	}

}
