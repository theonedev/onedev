package com.pmease.commons.git;

import static com.pmease.commons.git.Change.Status.ADDED;
import static com.pmease.commons.git.Change.Status.DELETED;
import static com.pmease.commons.git.Change.Status.MODIFIED;
import static com.pmease.commons.git.Change.Status.RENAMED;
import static com.pmease.commons.git.Change.Status.UNCHANGED;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.AddCommand;
import com.pmease.commons.git.command.AddNoteCommand;
import com.pmease.commons.git.command.AddSubModuleCommand;
import com.pmease.commons.git.command.AheadBehindCommand;
import com.pmease.commons.git.command.BlameCommand;
import com.pmease.commons.git.command.CalcMergeBaseCommand;
import com.pmease.commons.git.command.CheckoutCommand;
import com.pmease.commons.git.command.CherryPickCommand;
import com.pmease.commons.git.command.CloneCommand;
import com.pmease.commons.git.command.CommitCommand;
import com.pmease.commons.git.command.ConsumeCommand;
import com.pmease.commons.git.command.DeleteRefCommand;
import com.pmease.commons.git.command.FetchCommand;
import com.pmease.commons.git.command.InitCommand;
import com.pmease.commons.git.command.IsAncestorCommand;
import com.pmease.commons.git.command.IsBinaryCommand;
import com.pmease.commons.git.command.IsTreeLinkCommand;
import com.pmease.commons.git.command.ListBranchCommand;
import com.pmease.commons.git.command.ListChangedFilesCommand;
import com.pmease.commons.git.command.ListCherriesCommand;
import com.pmease.commons.git.command.ListFileChangesCommand;
import com.pmease.commons.git.command.ListHeadCommitCommand;
import com.pmease.commons.git.command.ListSubModulesCommand;
import com.pmease.commons.git.command.ListTagsCommand;
import com.pmease.commons.git.command.ListTreeCommand;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.MergeCommand;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.git.command.MoveCommand;
import com.pmease.commons.git.command.ParseRevisionCommand;
import com.pmease.commons.git.command.PullCommand;
import com.pmease.commons.git.command.PushCommand;
import com.pmease.commons.git.command.RemoveCommand;
import com.pmease.commons.git.command.ResetCommand;
import com.pmease.commons.git.command.ShowCommand;
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
		if (new ListBranchCommand(repoDir).call().contains(branchName))
			throw new GeneralException(String.format("Branch %s already exists.", branchName));

		new UpdateRefCommand(repoDir).refName(Git.REFS_HEADS + branchName).revision(commitHash)
				.oldRevision(GitUtils.NULL_SHA1).call();
		
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
			throw new GeneralException(String.format("Tag %s already exists.", tagName));

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

	/**
	 * List tree of specified revision under specified path.
	 * 
	 * @param revision
	 * 			revision to list the tree off
	 * @param path
	 * 			path to list tree for. Note that if path is not empty and does not end 
	 * 			with slash, this method will list the path itself, instead of listing 
	 * 			child entries under this path
	 * @return
	 */
	public List<TreeNode> listTree(String revision, @Nullable String path) {
		return new ListTreeCommand(repoDir).revision(revision).path(path).call();
	}
	
	/**
	 * List tree of fromRev, and mark differences from fromRev to toRev in the resulting tree.
	 * 
	 * @param fromRev
	 * 			revision to base the tree off
	 * @param toRev
	 * 			revision to calculate file differences since fromRev 
	 * @param path
	 * 			path to list tree under5
	 * @param changes
	 * 			all changes between fromRev and toRev. This param is passed for performance reasons, 
	 * 			in order not to calculate changes over and over again when expand nodes of a diff tree
	 * @return
	 * 			a list of tree node with appropriate actions set up to mark whether or not this 
	 * 			tree node is modified, added, deleted or remaining the same
	 */
	public List<Change> listTree(String fromRev, String toRev, @Nullable String path, @Nullable List<Change> changes) {
		if (path == null) 
			path = "";
		
		if (path.length() != 0 && !path.endsWith("/"))
			path += "/";
		
		if (changes == null)
			changes = listFileChanges(fromRev, toRev, path, true);

		List<Change> diffs = new ArrayList<>();
		
		Map<String, Change> changesMap = new HashMap<>();
		
		for (Change change: changes) { 
			changesMap.put(change.getPath(), change);
			if (change.getStatus() == RENAMED)
				changesMap.put(change.getOldPath(), change);
		}
		
		Set<String> nodePathsInToRev = new HashSet<>();
		for (TreeNode each: listTree(toRev, path))
			nodePathsInToRev.add(each.getPath());

		Set<String> coveredFiles = new HashSet<>();
		for (TreeNode treeNode: listTree(fromRev, path)) {
			if (FileMode.TREE.equals(treeNode.getMode())) {
				String treePath = treeNode.getPath() + "/";
				Change diff = null;
				for (String changedFile: changesMap.keySet()) {
					if (changedFile.startsWith(treePath)) {
						coveredFiles.add(changedFile);
						if (diff == null) {
							diff = new Change(MODIFIED, fromRev, toRev, treeNode.getPath(), treeNode.getPath(), 
									FileMode.TYPE_TREE, FileMode.TYPE_TREE);
						}
					}
				}
				if (diff != null) {
					if (!nodePathsInToRev.contains(treeNode.getPath()))
						diff = new Change(DELETED, fromRev, toRev, treeNode.getPath(), null,
								FileMode.TYPE_TREE, 0);
				} else {
					diff = new Change(UNCHANGED, fromRev, toRev, treeNode.getPath(), treeNode.getPath(), 
							FileMode.TYPE_TREE, FileMode.TYPE_TREE);
				}
				diffs.add(diff);
			} else {
				Change change = changesMap.get(treeNode.getPath());
				if (change != null) {
					if (change.getStatus() != RENAMED || change.getNewPath().equals(treeNode.getPath())) 
						diffs.add(change);
				} else {
					diffs.add(new Change(UNCHANGED, fromRev, toRev, treeNode.getPath(), treeNode.getPath(),  
							treeNode.getMode(), treeNode.getMode()));
				}
			}
		}

		Set<String> addedDirs = new HashSet<>();
		for (Map.Entry<String, Change> entry: changesMap.entrySet()) {
			String file = entry.getKey();
			Change change = entry.getValue();
			if ((change.getStatus() == ADDED || change.getStatus() == RENAMED && change.getNewPath().equals(file)) 
					&& !coveredFiles.contains(file) && file.startsWith(path)) {
				String relativePath = file.substring(path.length());
				int index = relativePath.indexOf('/');
				if (index != -1)
					addedDirs.add(path + relativePath.substring(0, index));
				else  
					diffs.add(change);
			}
		}
		
		for (String addedDir: addedDirs) 
			diffs.add(new Change(ADDED, fromRev, toRev, null, addedDir, 0, FileMode.TYPE_TREE));
		
		Collections.sort(diffs);
		return diffs;
	}
	
	public Git init(boolean bare) {
		new InitCommand(repoDir).bare(bare).call();
		return this;
	}

	public Git add(String... paths) {
		new AddCommand(repoDir).addPaths(paths).call();
		return this;
	}
	
	public Git rm(String... paths) {
		new RemoveCommand(repoDir).paths(paths).call();
		return this;
	}
	
	public Git mv(String source, String destination) {
		new MoveCommand(repoDir).source(source).destination(destination).call();
		return this;
	}

	public Git clone(String from, boolean bare, boolean shared, boolean noCheckout, @Nullable String branch) {
		new CloneCommand(repoDir).from(from).bare(bare).shared(shared).noCheckout(noCheckout).branch(branch).call();
		return this;
	}

	public Git clone(Git from, boolean bare, boolean shared, boolean noCheckout, @Nullable String branch) {
		new CloneCommand(repoDir)
				.from(from.repoDir.getAbsolutePath()).bare(bare)
				.shared(shared).noCheckout(noCheckout).branch(branch).call();
		return this;
	}

	public Git reset(@Nullable String mode, @Nullable String commit) {
		new ResetCommand(repoDir).mode(mode).commit(commit).call();
		return this;
	}

	public Git commit(String message, boolean add, boolean amend) {
		new CommitCommand(repoDir).message(message).amend(amend).call();
		return this;
	}

	public Collection<String> listChangedFiles(String fromRev, String toRev, @Nullable String path) {
		return new ListChangedFilesCommand(repoDir).fromRev(fromRev).toRev(toRev).path(path).call();
	}

	public List<Change> listFileChanges(String fromRev, String toRev, @Nullable String path,
			boolean findRenames) {
		return new ListFileChangesCommand(repoDir).fromRev(fromRev).toRev(toRev).path(path)
				.findRenames(findRenames).call();
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
	public Git updateRef(String refName, String revision, 
			@Nullable String oldRevision, @Nullable String reason) {
		new UpdateRefCommand(repoDir).refName(refName)
				.revision(revision).oldRevision(oldRevision)
				.reason(reason).call();
		return this;
	}
	
	public Git deleteRef(String refName, @Nullable String oldRevision, @Nullable String reason) {
		new DeleteRefCommand(repoDir).refName(refName).oldRevision(oldRevision).reason(reason).call();
		return this;
	}

	public String merge(String revision, @Nullable FastForwardMode fastForwardMode, 
			@Nullable String strategy, @Nullable String strategyOption, @Nullable String message) {
		return new MergeCommand(repoDir).revision(revision).fastForwardMode(fastForwardMode)
				.strategy(strategy).strategyOption(strategyOption).message(message).call();
	}

	public String squash(String revision, @Nullable String strategy, @Nullable String strategyOption, 
			@Nullable String message) {
		return new MergeCommand(repoDir).revision(revision).squash(true)
				.strategy(strategy).strategyOption(strategyOption).message(message).call();
	}

	public Git fetch(String from, String... refspec) {
		new FetchCommand(repoDir).from(from).refspec(refspec).call();
		return this;
	}
	
	public Git fetch(Git from, String... refspec) {
		new FetchCommand(repoDir).from(from.repoDir.getAbsolutePath()).refspec(refspec).call();
		return this;
	}

	public Git pull(String from, String... refspec) {
		new PullCommand(repoDir).from(from).refspec(refspec).call();
		return this;
	}

	public Git pull(Git from, String... refspec) {
		new PullCommand(repoDir).from(from.repoDir.getAbsolutePath()).refspec(refspec).call();
		return this;
	}

	public Git push(String to, String... refspec) {
		new PushCommand(repoDir).to(to).refspec(refspec).call();
		return this;
	}

	public Git push(Git to, String... refspec) {
		new PushCommand(repoDir).to(to.repoDir.getAbsolutePath()).refspec(refspec).call();
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
	
	/**
	 * List all local branches in git repository.

	 * @return
	 * 			a collection of branch names
	 */
	public Collection<String> listBranches() {
		return new ListBranchCommand(repoDir).call();
	}
	
	public Map<String, AheadBehind> getAheadBehinds(String baseRev, String... compareRevs) {
		AheadBehindCommand cmd = new AheadBehindCommand(repoDir);
		return cmd.baseCommit(baseRev).compareRevs(compareRevs).call();
	}
	
	/**
	 * List head commits of all local branches.

	 * @return
	 * 			a map from branch name to brief head commit
	 */
	public Map<String, BriefCommit> listHeadCommits() {
		return new ListHeadCommitCommand(repoDir).call();
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
	public Git consume(String revision, String path, StreamConsumer consumer) {
		new ConsumeCommand(repoDir).revision(revision).path(path).consumer(consumer).call();
		return this;
	}
	
	public byte[] show(String revision, String path) {
		return new ShowCommand(repoDir).revision(revision).path(path).call();
	}
	
	public byte[] readBlob(BlobIdent blob) {
		if (blob.mode == FileMode.TYPE_GITLINK) {
			String subModuleUrl = listSubModules(blob.revision).get(blob.path);
			Preconditions.checkNotNull(subModuleUrl);
			List<TreeNode> result = listTree(blob.revision, blob.path);
			return (subModuleUrl + ":" + result.iterator().next().getHash()).getBytes();
		} else {
			return show(blob.revision, blob.path);
		}
	}

	public List<Commit> log(@Nullable String fromRev, @Nullable String toRev, 
			@Nullable String path, int maxCount, int skip) {
		return new LogCommand(repoDir).fromRev(fromRev).toRev(toRev)
				.path(path).maxCount(maxCount).skip(skip).call();
	}
	
	public List<Commit> log(@Nullable Date sinceDate, @Nullable Date untilDate, 
			@Nullable String path, int maxCount, int skip) {
		return new LogCommand(repoDir).sinceDate(sinceDate).untilDate(untilDate)
				.path(path).maxCount(maxCount).skip(skip).call();
	}
	
	public Commit retrieveLastCommmit(String revision, @Nullable String path) {
		return log(null, revision, path, 1, 0).get(0);
	}
	
	public Git addNote(String object, String message) {
		new AddNoteCommand(repoDir).object(object).message(message).call();
		return this;
	}
	
	/**
	 * Display commit information for every region of specified commit and file. 
	 * 
	 * @param file
	 * 			file for blame
	 * @param commitHash
	 * 			commit hash of the file for blame
	 * @return
	 * 			map of commit hash to commit blame
	 */
	public Map<String, Blame> blame(String commitHash, String file) {
		return new BlameCommand(repoDir).commitHash(commitHash).file(file).call();
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
	
	public String cherryPick(String...revisions) {
		return new CherryPickCommand(repoDir).revisions(revisions).call();
	}

	public List<String> listCherries(String fromRev, String toRev) {
		return new ListCherriesCommand(repoDir).fromRev(fromRev).toRev(toRev).call();
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
